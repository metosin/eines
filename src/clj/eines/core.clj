(ns eines.core
  (:require [eines.pack.transit :as transit]
            [eines.pack.json :as json]
            [eines.pack.edn :as edn]))

(def sockets (atom {}))

(defn create-packers [{:keys [transit]}]
  {:transit+json [(transit/create-packer transit)
                  (transit/create-unpacker transit)]
   :json [json/pack json/unpack]
   :edn [edn/pack edn/unpack]})

(defn init-state [ch request send-fn out-middleware opts]
  (let [packers (create-packers opts)]
    (let [[pack unpack] (-> request
                            :params
                            (get "format")
                            keyword
                            (or :transit+json)
                            packers
                            (or (throw (ex-info "unknown format" {}))))]
      {:eines/request-headers (:headers request)
       :eines/state {:ch ch
                     :pack pack
                     :unpack unpack
                     :send-fn send-fn}
       :opened (System/currentTimeMillis)
       :send! (fn send!
                ([message]
                 (send! message nil nil))
                ([message response-fn]
                 (send! message response-fn nil))
                ([message response-fn timeout]
                 (let [message (cond-> message
                                       (nil? (:type message)) (assoc :type :eines.type/request)
                                       response-fn (assoc-in [:headers :eines/rsvp] {:response-fn response-fn
                                                                                     :timeout (or timeout 5000)}))]
                   (->> message
                        (out-middleware)
                        (pack)
                        (send-fn ch)))))})))

(defn on-open [out-middleware on-open-listener opts ch request send-fn]
  (swap! sockets assoc ch (init-state ch request send-fn out-middleware opts))
  (on-open-listener ch))

(defn on-close [on-close-listener ch]
  (doseq [on-close (-> @sockets (get ch) :on-closes vals)]
    (on-close))
  (on-close-listener ch)
  (swap! sockets dissoc ch))

(defn send-pong [state]
  (let [{:keys [send-fn ch pack]} (:eines/state state)]
    (send-fn ch (pack {:type :eines.type/pong}))))

(defn handle-inbound-message [on-message ch message]
  (let [state (get @sockets ch)
        message ((-> state :eines/state :unpack) message)]
    (case (:type message)
      :eines.type/ping (send-pong state)
      :eines.type/pong nil
      (:eines.type/request :eines.type/response) (on-message (-> message
                                                                 (merge state)
                                                                 (assoc :ch ch))))))

(defn make-outbound-handler [middleware]
  (->> middleware
       (keep :out)
       (reduce (fn
                 ([] identity)
                 ([acc middleware]
                  (middleware acc)))
               identity)))

(defn make-inbound-handler [middleware on-message]
  (->> middleware
       (keep :in)
       (reverse)
       (reduce (fn
                 ([] on-message)
                 ([acc middleware]
                  (middleware acc)))
               on-message)))

(defn handler-context
  "Options:
  - :middleware
  - :on-open
  - :on-close"
  ([on-message]
   (handler-context on-message nil))
  ([on-message {:keys [middleware] :as opts}]
   {:on-message (partial handle-inbound-message (make-inbound-handler middleware on-message))
    :on-open (partial on-open (make-outbound-handler middleware) (:on-open opts identity) opts)
    :on-close (partial on-close (:on-close opts identity))}))

(comment

  (-> @sockets count)

  (doseq [{:keys [send!]} (-> @sockets vals)]
    (send! {:body {:type :greetings
                   :greetings "Whassup?"}}
           (fn [response]
             (println "Client response:" (pr-str (:body response)))))))
