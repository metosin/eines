(ns eines.server.aleph
  (:require [aleph.http :as http]
            [clojure.tools.logging :as log]
            [manifold.deferred :as d]
            [manifold.stream :as s]))

(defn create-handler [{:keys [on-open on-close on-message]}]
  (fn [{:keys [uri] :as req}]
    (d/let-flow [conn (d/catch
                        (http/websocket-connection req)
                        (fn [_] nil))]
      (when conn
        (s/on-closed conn #(on-close conn))
        (on-open conn req s/put!)
        (s/consume #(on-message conn %) conn)))))
