(ns eines-example.message.login
  (:require [clojure.tools.logging :as log]
            [plumbing.core :refer [defnk]]))

(defnk login [[:body user password]]
  (log/infof "login: user=[%s], password=[%s]" user password)
  ; todo: check the password
  {:session {:user user}})

(defnk logout [[:session user]]
  (log/infof "logout: user=[%s]" user)
  ; todo: check the password
  {:session nil})

(def handlers {:login login
               :logout logout})
