(ns eines-example.message.favorite
  (:require [clojure.tools.logging :as log]
            [plumbing.core :refer [defnk]]))

(defnk favorite [[:session user] [:body favorite]]
  (log/infof "favorite: user=[%s], favorite=[%s]" user favorite))

(defnk mood [[:session user] [:body mood]]
  (log/infof "mood: user=[%s], favorite=[%s]" user mood)
  {:body (str "Great to hear that you feel " mood)})

(def handlers {:favorite favorite
               :mood mood})
