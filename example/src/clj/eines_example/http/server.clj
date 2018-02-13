(ns eines-example.http.server
  (:require [clojure.tools.logging :as log]
            [mount.core :as mount]
            [immutant.web :as immutant]
            [ring.middleware.content-type :refer [wrap-content-type]]
            [ring.middleware.params :refer [wrap-params]]
            [ring.util.response :as response]
            [eines-example.http.index :as index]
            [eines-example.http.static :as static]
            [eines.core :as eines]
            [eines.server.immutant :as eines-immutant]
            [eines.middleware.rsvp :as rsvp]
            [eines.middleware.session :as session]
            [eines-example.message.handler :as handler]))

(defn create-handler []
  (some-fn (-> (eines/handler-context handler/handle-message
                                      {:middleware [(session/session-middleware)
                                                    (rsvp/rsvp-middleware)]})
               (eines-immutant/create-handler)
               (wrap-params))
           (-> (static/create-handler)
               (wrap-content-type))
           (index/create-handler)
           (constantly (response/not-found "Not found"))))

(defn start-server [config]
  (log/infof "Starting HTTP server, listening port %d..." (:port config))
  (immutant/run (create-handler) config))

(defn stop-server [server]
  (log/info "Stopping HTTP server...")
  (immutant/stop server))

(mount/defstate server
  :start (start-server {:port 3030, :path "/"})
  :stop (stop-server server))
