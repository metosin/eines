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

(def message-handler (-> handler/handle-message
                         (rsvp/wrap-rsvp)
                         (session/wrap-session)))

(defn create-handler []
  (some-fn (-> (eines/handler-context message-handler)
               (eines-immutant/create-handler)
               (wrap-params))
           (-> (static/create-handler)
               (wrap-content-type))
           (index/create-handler)
           (constantly (response/not-found "Not found"))))

(defn start-server []
  (log/infof "Starting HTTP server...")
  (immutant/run (create-handler) {:port 3030
                                  :path "/"}))

(defn stop-server [server]
  (log/info "Stopping HTTP server...")
  (immutant/stop server))

(mount/defstate server
  :start (start-server)
  :stop (stop-server server))
