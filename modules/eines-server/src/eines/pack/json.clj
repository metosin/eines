(ns eines.pack.json
  (:require [cheshire.core :as c]))

(defn pack ^String [message]
  (when message
    (c/generate-string message)))

(defn unpack [^String message]
  (when message
    (c/parse-string message keyword)))
