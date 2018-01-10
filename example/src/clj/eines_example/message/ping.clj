(ns eines-example.message.ping
  (:require [clojure.tools.logging :as log]
            [plumbing.core :refer [defnk]]))

(defnk ping [body]
  (log/infof "ping: body=[%s], thread=[%s]" body (-> (Thread/currentThread) (.getName)))
  {:body {:ping "pong!"}})

(def handlers {:ping ping})
