(ns eines-example.message.handler
  (:require [eines-example.message.login :as login]
            [eines-example.message.favorite :as favorite]
            [eines-example.message.ping :as ping]
            [clojure.tools.logging :as log]))

(def handlers (merge login/handlers
                     favorite/handlers
                     ping/handlers))

(defn not-found [message]
  (log/error "unknown message type:" (-> message :body :type)))

(defn handle-message [message]
  (let [message-type (-> message :body :type)
        handler (get handlers message-type not-found)]
    (handler message)))
