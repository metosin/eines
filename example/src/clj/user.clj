(ns user
  (:require [clojure.tools.logging :as log]
            [clojure.tools.namespace.repl :as tns]
            [clojure.java.classpath :as cp]
            [clojure.string :as str]
            [eines-example.core :as core]))

(defn cljs-generated? [^java.io.File f]
  (-> f .getAbsolutePath (str/ends-with? "target/dev/resources")))

(apply tns/set-refresh-dirs (remove cljs-generated? (cp/classpath-directories)))

(def start core/start)
(def stop core/stop)

(defn go []
  (start)
  :ready)

(defn reset []
  (stop)
  (tns/refresh :after 'user/go))
