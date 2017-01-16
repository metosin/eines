(ns eines.server.immutant
  (:require [clojure.tools.logging :as log]
            [immutant.web.async :as async]
            [eines.pack.transit :as transit]))

(defn create-handler [{:keys [on-message add-socket remove-socket]}]
  (fn [{:keys [websocket? uri] :as request}]
    (when (and websocket? (= uri "/ws"))
      (async/as-channel request {:on-open (fn [ch]
                                            (add-socket ch request async/send!))
                                 :on-close (fn [ch _]
                                             (remove-socket ch))
                                 :on-error (fn [ch e]
                                             (log/error e "on-error")
                                             (async/close ch))
                                 :on-message (fn [ch message]
                                               (on-message ch message))
                                 :timeout 15000}))))
