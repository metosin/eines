(ns eines.middleware.session
  (:require [eines.core :as c]))

(defn session-middleware []
  {:in (fn [handler]
         (fn [message]
           (let [response (handler message)]
             (if-let [session (-> response :session)]
               (do (swap! c/sockets update (-> message :eines/state :ch) assoc :session session)
                   (dissoc response :session))
               response))))})
