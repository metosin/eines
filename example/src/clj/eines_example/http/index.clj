(ns eines-example.http.index
  (:require [ring.util.response :as response]
            [hiccup.core :as hiccup]
            [hiccup.page :as page]))

(defn make-index-response []
  (-> (hiccup/html
        (page/html5
          [:head
           [:title "Eines example"]
           [:meta {:charset "utf-8"}]
           (page/include-css "css/style.css")]
          [:body
           [:div#app
            [:div.loading
             [:h1 "Loading..."]]]
           (page/include-js "js/main.js")]))
      (response/response)
      (response/content-type "text/html; charset=utf-8")))

(defn create-handler []
  (let [response (make-index-response)]
    (fn [request]
      (if (and (-> request :request-method (= :get))
               (-> request :uri (= "/")))
        response))))
