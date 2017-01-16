(ns eines.client
  (:require [cognitect.transit :as t]))

;;
;; Defaults:
;;

(def default-url (-> js/window.location.protocol
                     {"http:" "ws:", "https:" "wss:"}
                     (str "//" js/window.location.host "/ws")))

(def default-options {:on-message identity
                      :on-connect identity
                      :on-close identity
                      :on-error identity
                      :url default-url
                      :format :transit+json})

;;
;; Helpers:
;;

(defn ^:private close! [socket]
  (when socket
    (.close socket))
  nil)

(defn ^:private update-socket! [prev-socket new-socket]
  (close! prev-socket)
  new-socket)

(defn ^:private update-timeout! [prev-timeout f timeout-ms]
  (when prev-timeout
    (js/clearTimeout prev-timeout))
  (js/setTimeout f timeout-ms))

;;
;; Message pack & unpack with transit:
;;

(def ^:private pack
  (let [writer (t/writer :json)]
    (fn [message]
      (t/write writer message))))

(def ^:private unpack
  (let [reader (t/reader :json)]
    (fn [message]
      (t/read reader message))))

;;
;; State:
;;

(def ^:private state (atom {:socket nil
                            :rsvp {:request-id 0
                                   :requests {}}}))

;;
;; Send message:
;;

(defn rsvp-response [message]
  (let [response-id (-> message :headers :eines/rsvp-response-id)]
    (swap! state update-in [:rsvp :requests]
           (fn [requests]
             (when-let [{:keys [response-fn timeout]} (get requests response-id)]
               (js/clearTimeout timeout)
               (response-fn message))
             (dissoc requests response-id)))))

(defn set-timeout [request-id timeout]
  (js/setTimeout
    (partial rsvp-response {:type :eines.type/timeout
                            :headers {:eines/rsvp-response-id request-id}})
    timeout))

(defn push-rsvp [rsvp response-fn timeout]
  (let [request-id (-> rsvp :request-id inc)]
    (-> rsvp
        (assoc :request-id request-id)
        (assoc-in [:requests request-id] {:response-fn response-fn
                                          :timeout (set-timeout request-id timeout)}))))

(defn rsvp-request-id [response-fn timeout]
  (if response-fn
    (-> (swap! state update :rsvp push-rsvp response-fn timeout)
        :rsvp
        :request-id)))

(defn send!
  ([message] (send! message nil nil))
  ([message response-fn] (send! message response-fn 5000))
  ([message response-fn timeout]
   (when-let [socket (:socket @state)]
     (let [message {:type :eines.type/request
                    :body message}
           message (if response-fn
                     (assoc-in message [:headers :eines/rsvp-request-id] (rsvp-request-id response-fn timeout))
                     message)]
       (.send socket (pack message))))))

;;
;; Connection polling:
;;

(defn make-url [url format]
  (str url "?format=" (js/encodeURIComponent (name format))))

(defn ^:private set-socket! [new-socket]
  (swap! state update :socket update-socket! new-socket))

(defn ^:private handle-message [e socket on-message]
  (let [message (->> e .-data unpack)]
    (case (:type message)
      :eines.type/pong nil
      :eines.type/ping (.send socket (pack {:type :eines.type/pong}))
      :eines.type/request (on-message message)
      :eines.type/response (rsvp-response message))
    nil))

(defn ^:private connect! []
  (let [{:keys [url format on-message on-connect on-close on-error]} @state
        socket (js/WebSocket. (make-url url format))]
    (set! (.-onopen socket) (fn [_]
                              (set-socket! socket)
                              (on-connect)))
    (set! (.-onclose socket) (fn [_]
                               (set-socket! nil)
                               (on-close)))
    (set! (.-onerror socket) (fn [_]
                               (set-socket! nil)
                               (on-error)))
    (set! (.-onmessage socket) (fn [e]
                                 (handle-message e socket on-message)))))

(defn ^:private poll-connection! []
  (let [socket (:socket @state)]
    (if socket
      (.send socket (pack {:type :eines.type/ping}))
      (connect!))
    (swap! state update :timeout update-timeout! poll-connection! (if socket 5000 1000))))

;;
;; Init WebSocket:
;;

(defn ^:private init-state [{prev-socket :socket} opts]
  (close! prev-socket)
  opts)

(defn init! [opts]
  (swap! state init-state (merge default-options opts))
  (poll-connection!))

;;
;; Helpers:
;;

(defn timeout? [message]
  (-> message :type (= :eines.type/timeout)))

(defn success? [message]
  (-> message :type #{:eines.type/response :eines.type/request}))
