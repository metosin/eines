(ns eines.core
  (:require [eines.pack.transit :as transit]
            [eines.pack.json :as json]
            [eines.pack.edn :as edn]))

(def sockets (atom {}))

(def packers {:transit+json [transit/pack transit/unpack]
              :json [json/pack json/unpack]
              :edn [edn/pack edn/unpack]})

(defn init-state [ch request send-fn]
  (let [[pack unpack] (-> request
                          :params
                          (get "format")
                          keyword
                          (or :transit+json)
                          packers
                          (or (throw (ex-info "unknown format" {}))))
        send! (fn [message]
                (send-fn ch (pack message)))]
    {:eines/state {:ch ch
                   :pack pack
                   :unpack unpack}
     :send! send!}))

(defn add-socket [ch request send-fn]
  (swap! sockets assoc ch (init-state ch request send-fn)))

(defn remove-socket [ch]
  (swap! sockets dissoc ch))

(defn handle-message [on-message ch message]
  (let [state (get @sockets ch)
        message ((-> state :eines/state :unpack) message)]
    (case (:type message)
      :eines.type/ping ((:send! state) {:type :eines.type/pong})
      :eines.type/pong nil
      :eines.type/request (on-message (merge message state)))))

(defn handler-context [on-message]
  {:on-message (partial handle-message on-message)
   :add-socket add-socket
   :remove-socket remove-socket})

(comment
  (doseq [send! (->> @sockets vals (map :send!))]
    (send! {:foo "bar"}))
  )