(defproject metosin/eines-aleph "0.1.0-SNAPSHOT"
  :description "Simple clj/cljs library for WebSocket communication"
  :url "https://github.com/metosin/eines"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :plugins [[lein-parent "0.3.2"]]
  :parent-project {:path "../../project.clj"
                   :inherit [:deploy-repositories :managed-dependencies]}
  :dependencies [[aleph]
                 [org.clojure/tools.logging]])
