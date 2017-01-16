(ns eines.middleware.rsvp)

(defn wrap-rsvp [handler]
  (fn [message]
    (if-let [response (handler message)]
      (if-let [request-id (-> message :headers :eines/rsvp-request-id)]
        (let [send! (-> message :send!)]
          (-> response
              (assoc-in [:headers :eines/rsvp-response-id] request-id)
              (assoc :type :eines.type/response)
              (send!)))
        response))))
