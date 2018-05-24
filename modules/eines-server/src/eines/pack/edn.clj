(ns eines.pack.edn
  (:require [clojure.edn :as edn]))

(defn pack ^String [message]
  (when message
    (pr-str message)))

(defn unpack [^String message]
  (when message
    (edn/read-string message)))
