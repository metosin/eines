(defproject metosin/eines "0.0.6"
  :description "Simple clj/cljs library for WebSocket communication"
  :url "https://github.com/metosin/eines"
  :license {:name "Eclipse Public License", :url "http://www.eclipse.org/legal/epl-v10.html"}

  :dependencies [; Client
                 [com.cognitect/transit-cljs "0.8.239"]
                 ; Server
                 [org.clojure/tools.logging "0.3.1"]
                 [com.cognitect/transit-clj "0.8.297"]
                 [cheshire "5.6.3"]
                 ; Optionals
                 [org.immutant/web "2.1.6" :scope "provided"]]

  :profiles {:dev {:dependencies [[org.clojure/clojure "1.8.0"]
                                  [org.clojure/clojurescript "1.9.293" :exclusions [com.google.code.findbugs/jsr305]]
                                  [org.immutant/web "2.1.6"]]}}

  :global-vars {*warn-on-reflection* true}

  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :test-paths ["test/clj" "test/cljc"])
