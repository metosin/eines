(ns eines-example.http.static
  (:require [ring.util.response :as response]
            [clojure.string :as str]))

(defn create-handler []
  (fn [{:keys [request-method uri]}]
    (if (= request-method :get)
      (response/resource-response uri {:root "/public"}))))
