(ns eines.server.immutant
  (:require [clojure.tools.logging :as log]
            [immutant.web.async :as async])
  (:import (io.undertow.server HttpServerExchange)))

(defn create-handler [{:keys [on-open on-close on-message]}]
  (fn [{:keys [websocket? uri] :as request}]
    (when (and websocket? (= uri "/ws"))
      (async/as-channel request {:on-open (fn [ch]
                                            (on-open ch request async/send!))
                                 :on-close (fn [ch _]
                                             (on-close ch))
                                 :on-error (fn [ch e]
                                             (log/error e "on-error")
                                             (async/close ch))
                                 :on-message (let [^HttpServerExchange exchange (-> request :server-exchange)]
                                               (fn [ch message]
                                                 (if (.isInIoThread exchange)
                                                   (.dispatch exchange ^Runnable ^:once (fn [] (on-message ch message)))
                                                   (on-message ch message))))}))))
