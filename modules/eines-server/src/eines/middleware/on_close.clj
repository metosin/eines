(ns eines.middleware.on-close
  (:require [eines.core :as c]))

(defn add-on-close [listeners key listener]
  (assoc listeners key listener))

(defn remove-on-close [listeners key listsner]
  (dissoc listeners key))

(defn on-close-middleware []
  {:in (fn [handler]
         (fn [message]
           (let [response (handler message)]
             (if-let [{:keys [op key listener]} (-> response :on-close)]
               (do (swap! c/sockets update-in [(-> message :eines/state :ch) :on-closes]
                          (case op
                            :add add-on-close
                            :remove remove-on-close)
                          key
                          listener)
                   (dissoc response :on-close))
               response))))})
