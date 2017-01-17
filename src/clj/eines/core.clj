(ns eines.core
  (:require [eines.pack.transit :as transit]
            [eines.pack.json :as json]
            [eines.pack.edn :as edn]))

(def sockets (atom {}))

(def packers {:transit+json [transit/pack transit/unpack]
              :json [json/pack json/unpack]
              :edn [edn/pack edn/unpack]})

(defn init-state [ch request send-fn out-middleware]
  (let [[pack unpack] (-> request
                          :params
                          (get "format")
                          keyword
                          (or :transit+json)
                          packers
                          (or (throw (ex-info "unknown format" {}))))
        send! (fn [message & [response-fn timeout]]
                (->> (if response-fn
                       (assoc-in message [:headers :eines/rsvp] {:response-fn response-fn
                                                                 :timeout (or timeout 5000)})
                       message)
                     (out-middleware)
                     (pack)
                     (send-fn ch)))]
    {:eines/state {:ch ch
                   :pack pack
                   :unpack unpack
                   :send-fn send-fn}
     :send! send!
     :opened (System/currentTimeMillis)}))

(defn add-socket [out-middleware ch request send-fn]
  (swap! sockets assoc ch (init-state ch request send-fn out-middleware)))

(defn remove-socket [ch]
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
      (:eines.type/request :eines.type/response) (on-message (merge message state)))))

(defn make-outbound-handler [middlewares]
  (->> middlewares
       (keep :out)
       (reduce (fn
                 ([] identity)
                 ([acc middleware]
                  (middleware acc)))
               identity)))

(defn make-inbound-handler [middlewares on-message]
  (->> middlewares
       (keep :in)
       (reverse)
       (reduce (fn
                 ([] on-message)
                 ([acc middleware]
                  (middleware acc)))
               on-message)))

(defn handler-context [on-message & [{:keys [middlewares]}]]
  {:on-message (partial handle-inbound-message (make-inbound-handler middlewares on-message))
   :add-socket (partial add-socket (make-outbound-handler middlewares))
   :remove-socket remove-socket})

(comment

  (-> @sockets count)

  (doseq [{:keys [send!]} (-> @sockets vals)]
    (send! {:type :eines.type/request
            :body {:type :greetings
                   :greetings "Whassup?"}}
           (fn [response]
             (println "Client response:" (pr-str (:body response))))))

  )