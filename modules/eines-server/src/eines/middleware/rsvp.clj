(ns eines.middleware.rsvp
  (:require [clojure.tools.logging :as log]))

(defn- rsvp-completed [requests request-id response]
  (let [{:keys [future response-fn]} (get requests request-id)]
    (if future
      (future-cancel future))
    (if response-fn
      (response-fn response)))
  (dissoc requests request-id))

(defn- start-timeout [rsvp-requests request-id rsvp-ctx]
  (future
    (Thread/sleep (:timeout rsvp-ctx))
    (send rsvp-requests rsvp-completed request-id {:type :eines.type/timeout})))

(defn- rsvp-request-id [message]
  (-> message :headers :eines/rsvp-request-id))

(defn- rsvp-response-id [message]
  (-> message :headers :eines/rsvp-response-id))

(defn rsvp-middleware
  ([] (rsvp-middleware [:body]))
  ([response-keys]
   {:pre [(->> response-keys seq)
          (->> response-keys (every? keyword?))]}
   (let [next-request-id (let [id (atom 0)]
                           (fn [] (swap! id inc)))
         rsvp-requests (agent {} :error-handler (fn [_ e]
                                                  (log/error e "unexpected error in rsvp agent")))]
     {:in (fn [handler]
            (fn [message]
              (cond
                (rsvp-request-id message) (let [send! (-> message :send!)
                                                response (handler message)]
                                            (-> response
                                                (select-keys response-keys)
                                                (assoc-in [:headers :eines/rsvp-response-id] (rsvp-request-id message))
                                                (assoc :type :eines.type/response)
                                                (send!))
                                            response)
                (rsvp-response-id message) (do (send rsvp-requests
                                                     rsvp-completed
                                                     (rsvp-response-id message)
                                                     message)
                                               nil)
                :else (handler message))))
      :out (fn [handler]
             (fn [message]
               (handler (if-let [rsvp-ctx (-> message :headers :eines/rsvp)]
                          (let [request-id (next-request-id)
                                timeout (start-timeout rsvp-requests request-id rsvp-ctx)]
                            (send rsvp-requests assoc request-id (assoc rsvp-ctx :future timeout))
                            (-> message
                                (update :headers dissoc :eines/rsvp)
                                (update :headers assoc :eines/rsvp-request-id request-id)))
                          message))))})))
