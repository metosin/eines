(ns eines.pack.transit
  (:require [cognitect.transit :as t])
  (:import (java.io ByteArrayOutputStream ByteArrayInputStream)
           (java.nio.charset StandardCharsets)))

(defn create-packer [{:keys [writer]}]
  (fn ^String [message]
    (when message
      (let [out (ByteArrayOutputStream. 4096)
            w (t/writer out :json writer)]
        (t/write w message)
        (-> out
            (.toByteArray)
            (String. StandardCharsets/UTF_8))))))

(defn create-unpacker [{:keys [reader]}]
  (fn [^String message]
    (when message
      (-> message
          (.getBytes StandardCharsets/UTF_8)
          (ByteArrayInputStream.)
          (t/reader :json reader)
          (t/read)))))
