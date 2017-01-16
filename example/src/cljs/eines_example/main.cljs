(ns eines-example.main
  (:require [reagent.core :as r]
            [eines.client :as ws]
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
                          (ws/send! {:type :favorite
                                     :favorite favorite}))}
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
                          (ws/send! {:type :mood
                                     :mood mood}
                                    (fn [response]
                                      (if (ws/success? response)
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

(defn main []
  (ws/init! {:on-message (fn [message]
                           (js/console.log "message:" message))
             :on-connect (fn []
                           (js/console.log "connected")
                           (swap! app-state assoc :connected? true)
                           (ws/send! {:type :login
                                      :user "number 6"
                                      :password "secret"}))
             :on-close (fn []
                         (swap! app-state assoc :connected? false)
                         (js/console.log "closed"))
             :on-error (fn []
                         (swap! app-state assoc :connected? false)
                         (js/console.log "error"))})
  (r/render [main-view] (.getElementById js/document "app")))

(main)
