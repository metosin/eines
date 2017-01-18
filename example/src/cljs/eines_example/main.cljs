(ns eines-example.main
  (:require [reagent.core :as r]
            [eines.client :as eines]
            [clojure.string :as str]))

(defonce app-state (r/atom {}))

(defn main-view []
  (let [{:keys [connected? favorite mood mood-response]} @app-state]
    [:div
     [:h1 "Eines example"]
     [:div.connected
      [:span "Connection status:"]
      [:span (if connected? "Connected" "Connecting...")]]
     [:form {:on-submit (fn [e]
                          (.preventDefault e)
                          (eines/send! {:body {:type :favorite
                                               :favorite favorite}}))}
      [:h2 "Favourite eines"]
      [:input {:type "text"
               :placeholder "Your favorite eines?"
               :value favorite
               :on-change (fn [e] (->> e .-target .-value (swap! app-state assoc :favorite)))}]
      [:button {:type "submit"
                :disabled (or (not connected?)
                              (str/blank? favorite))}
       "Send"]]
     [:form {:on-submit (fn [e]
                          (.preventDefault e)
                          (eines/send! {:body {:type :mood
                                               :mood mood}}
                                       (fn [response]
                                         (if (eines/success? response)
                                           (swap! app-state assoc :mood-response (:body response))
                                           (js/console.log "fail:" (pr-str response))))))}
      [:h2 "Your mood"]
      [:input {:type "text"
               :placeholder "Your mood?"
               :value mood
               :on-change (fn [e] (->> e .-target .-value (swap! app-state assoc :mood)))}]
      [:button {:type "submit"
                :disabled (or (not connected?)
                              (str/blank? mood))}
       "Send"]
      (if mood-response
        [:div.mood-response
         [:span "Server response:"]
         [:span mood-response]])]]))

(defmulti on-message (comp :type :body))

(defmethod on-message :default [message]
  (js/console.warn "unknown message:" message))

(defmethod on-message :greetings [message]
  (js/console.log "Server sent greetings:" (-> message :body :greetings))
  {:body "Thanks for asking, I'm fine"})

(defn main []
  (eines/init! {:on-message on-message
                :on-connect (fn []
                              (swap! app-state assoc :connected? true)
                              (eines/send! {:body {:type :login
                                                   :user "user"
                                                   :password "password"}}))
                :on-close (fn []
                            (swap! app-state assoc :connected? false))
                :on-error (fn []
                            (swap! app-state assoc :connected? false))})
  (r/render [main-view] (.getElementById js/document "app")))

(main)
