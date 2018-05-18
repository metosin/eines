(defproject metosin/eines-parent "0.0.10-SNAPSHOT"
  :description "Simple clj/cljs library for WebSocket communication"
  :url "https://github.com/metosin/eines"
  :license {:name "Eclipse Public License", :url "http://www.eclipse.org/legal/epl-v10.html"}

  :managed-dependencies [[metosin/eines "0.0.10-SNAPSHOT"]
                         [metosin/eines-client "0.0.10-SNAPSHOT"]
                         [metosin/eines-immutant "0.0.10-SNAPSHOT"]
                         [metosin/eines-server "0.0.10-SNAPSHOT"]
                         [metosin/eines-aleph "0.0.10-SNAPSHOT"]

                         [aleph "0.4.5-alpha6"]
                         [com.cognitect/transit-cljs "0.8.243"]
                         [org.clojure/tools.logging "0.4.0"]
                         [com.cognitect/transit-clj "0.8.300"]
                         [cheshire "5.8.0"]
                         [org.immutant/web "2.1.10"]]

  :profiles {:dev {:source-paths ["modules/eines/src"
                                  "modules/eines-client/src"
                                  "modules/eines-immutant/src"
                                  "modules/eines-server/src"
                                  "modules/eines-aleph/src"]
                   :dependencies [[metosin/eines]
                                  [org.clojure/clojure "1.9.0"]
                                  [org.clojure/clojurescript "1.9.946" :exclusions [com.google.code.findbugs/jsr305]]]
                   :global-vars {*warn-on-reflection* true}}}

  :test-paths ["test/clj" "test/cljc"])
