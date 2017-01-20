(defproject metosin/eines-example "0.0.0-SNAPSHOT"
  :dependencies [[org.clojure/clojure "1.8.0"]
                 [org.clojure/clojurescript "1.9.293" :exclusions [com.google.code.findbugs/jsr305]]

                 ;; Work flow:
                 [org.clojure/tools.namespace "0.3.0-alpha3"]

                 ;; Eines:
                 [metosin/eines "0.0.5-SNAPSHOT"]

                 ;; Server:
                 [mount "0.1.11"]
                 [prismatic/plumbing "0.5.3"]
                 [org.immutant/web "2.1.6"]

                 ;; Client:
                 [reagent "0.6.0"]


                 ;; Logging:
                 [org.clojure/tools.logging "0.3.1"]
                 [org.slf4j/jcl-over-slf4j "1.7.22"]
                 [org.slf4j/jul-to-slf4j "1.7.22"]
                 [org.slf4j/log4j-over-slf4j "1.7.22"]
                 [ch.qos.logback/logback-classic "1.1.8" :exclusions [org.slf4j/slf4j-api]]

                 ;; Frontend development
                 [binaryage/devtools "0.8.3"]]

  :source-paths ["src/clj" "src/cljc"]
  :test-paths ["test/clj" "test/cljc"]

  :plugins [[lein-pdo "0.1.1"]
            [deraen/lein-sass4clj "0.3.0"]
            [lein-cljsbuild "1.1.4"]]

  :profiles {:dev {:resource-paths ["target/dev/resources"]
                   :sass {:target-path "target/dev/resources/public/css"}
                   :plugins [[lein-figwheel "0.5.8" :exclusions [org.clojure/clojure]]]}
             :prod {:resource-paths ["target/prod/resources"]
                    :sass {:target-path "target/prod/resources/public/css"}
                    :aot [backend.main]
                    :main backend.main
                    :uberjar-name "eines-example.jar"}}

  :sass {:source-paths ["src/sass"]
         :target-path "target/dev/resources/public/css"
         :source-map true
         :output-style :compressed}

  :figwheel {:css-dirs ["target/dev/resources/public/css"]
             :repl false
             :server-port 3452}

  :cljsbuild {:builds [{:id "dev"
                        :source-paths ["src/cljc" "src/cljs" "../src/cljc" "../src/cljs"]
                        :figwheel true
                        :compiler {:main eines-example.main
                                   :asset-path "js/out"
                                   :output-to "target/dev/resources/public/js/main.js"
                                   :output-dir "target/dev/resources/public/js/out"
                                   :source-map-timestamp true
                                   :closure-defines {goog.DEBUG true}
                                   :preloads [devtools.preload]
                                   :external-config {:devtools/config {:features-to-install [:formatters :hints]}}}}
                       {:id "prod"
                        :source-paths ["src/cljc" "src/cljs"]
                        :compiler {:main eines-example.main
                                   :optimizations :advanced
                                   :output-to "target/prod/resources/public/js/main.js"
                                   :output-dir "target/prod/resources/public/js/out"
                                   :closure-defines {goog.DEBUG false}}}]}

  :auto-clean false

  :aliases {"dev" ["do" "clean"
                   ["pdo" ["sass4clj" "auto"] ["figwheel"]]]
            "prod" ["with-profile" "prod" "do"
                    "clean"
                    ["sass4clj" "once"]
                    ["cljsbuild" "once" "prod"]
                    "uberjar"]})
