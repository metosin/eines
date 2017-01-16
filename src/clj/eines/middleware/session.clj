(ns eines.middleware.session
  (:require [eines.core :as c]))

(defn wrap-session [handler]
  (fn [message]
    (let [response (handler message)]
      (when-let [session (-> response :session)]
        (swap! c/sockets update (-> message :eines/state :ch) assoc :session session))
      response)))
