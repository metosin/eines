(ns eines.impl
  (:require [cognitect.transit :as t]))

;;
;; State:
;;

(defonce state (atom {:socket nil
                      :rsvp {:request-id 0
                             :requests {}}}))

;;
;; Message pack & unpack with transit:
;;

(def pack
  (let [writer (t/writer :json)]
    (fn [message]
      (t/write writer message))))

(def unpack
  (let [reader (t/reader :json)]
    (fn [message]
      (t/read reader message))))

;;
;; RSVP stuff:
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
  (-> (swap! state update :rsvp push-rsvp response-fn timeout)
      :rsvp
      :request-id))

;;
;; Connection polling:
;;

(defn deliver [message on-message]
  (let [response (on-message message)]
    (if-let [rsvp-request-id (-> message :headers :eines/rsvp-request-id)]
      (if-let [socket (:socket @state)]
        (.send socket (-> response
                          (assoc-in [:headers :eines/rsvp-response-id] rsvp-request-id)
                          (assoc :type :eines.type/response)
                          (pack)))))))

(defn handle-message [e socket on-message]
  (let [message (->> e .-data unpack)]
    (case (:type message)
      :eines.type/pong nil
      :eines.type/ping (.send socket (pack {:type :eines.type/pong}))
      :eines.type/request (deliver message on-message)
      :eines.type/response (rsvp-response message))
    nil))

(defn ping! []
  (if-let [socket (:socket @state)]
    (.send socket (pack {:type :eines.type/ping}))))

(defn set-socket! [state new-socket interval-fn interval-ms]
  (when-let [prev-interval (:interval state)]
    (js/clearInterval prev-interval))
  (when-let [old-socket (:socket state)]
    (.close old-socket))
  (assoc state :socket new-socket
               :interval (js/setInterval interval-fn interval-ms)))

(defn make-url [url format]
  (str url "?format=" (js/encodeURIComponent (name format))))

(defn connect! []
  (let [{:keys [url format on-message on-connect on-close on-error]} @state
        socket (js/WebSocket. (make-url url format))]
    (set! (.-onopen socket) (fn [_]
                              (swap! state set-socket! socket ping! 1000)
                              (on-connect)))
    (set! (.-onclose socket) (fn [_]
                               (swap! state set-socket! nil connect! 5000)
                               (on-close)))
    (set! (.-onerror socket) (fn [_]
                               (.close socket)
                               (on-error)))
    (set! (.-onmessage socket) (fn [e]
                                 (handle-message e socket on-message)))))