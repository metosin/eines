(defproject metosin/eines-immutant "0.0.10-SNAPSHOT"
  :description "Simple clj/cljs library for WebSocket communication"
  :url "https://github.com/metosin/eines"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-parent "0.3.2"]]
  :parent-project {:path "../../project.clj"
                   :inherit [:deploy-repositories :managed-dependencies]}
  :dependencies [[org.clojure/tools.logging]
                 [org.immutant/web]])
