(ns eines.client
  (:require [cognitect.transit :as t]
            [eines.impl :as i]))

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
;; Send message to server:
;;

(defn send!
  ([message]
   (send! message nil nil))
  ([message response-fn]
   (send! message response-fn 5000))
  ([message response-fn timeout]
   (let [{:keys [socket pack]} @i/state]
     (if socket
       (let [message (assoc message :type :eines.type/request)
             message (if response-fn
                       (assoc-in message [:headers :eines/rsvp-request-id] (i/rsvp-request-id response-fn timeout))
                       message)]
         (.send socket (pack message)))
       (js/console.error "eines.client/send!: socket is closed")))))

;;
;; Init WebSocket:
;;

(defn init! [opts]
  (let [opts (merge default-options opts)
        pack (i/create-packer (-> opts :transit :writer))
        unpack (i/create-unpacker (-> opts :transit :reader))]
    (swap! i/state i/reset-state (merge opts {:pack pack, :unpack unpack})))
  (i/connect!))

;;
;; Helpers:
;;

(defn timeout? [message]
  (-> message :type (= :eines.type/timeout)))

(defn success? [message]
  (-> message :type #{:eines.type/response :eines.type/request} boolean))
