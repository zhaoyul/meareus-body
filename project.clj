(defproject size-measure "0.1.0-SNAPSHOT"

  :description "FIXME: write description"
  :url "http://example.com/FIXME"

  :dependencies [[ch.qos.logback/logback-classic "1.2.3"]
                 [cheshire "5.8.1"]
                 [clojure.java-time "0.3.2"]
                 [com.cognitect/transit-clj "0.8.313"]
                 [com.google.javascript/closure-compiler-unshaded "v20190618" :scope "provided"]
                 [cprop "0.1.14"]
                 [expound "0.7.2"]
                 [funcool/struct "1.4.0"]
                 [luminus-jetty "0.1.7"]
                 [luminus-transit "0.1.1"]
                 [luminus/ring-ttl-session "0.3.3"]
                 [markdown-clj "1.10.0"]
                 [metosin/muuntaja "0.6.4"]
                 [metosin/reitit "0.3.9"]
                 [metosin/ring-http-response "0.9.1"]
                 [mount "0.1.16"]
                 [nrepl "0.6.0"]
                 [org.clojure/clojure "1.10.1"]
                 [org.clojure/clojurescript "1.10.520" :scope "provided"]
                 [org.clojure/core.async "0.4.500"]
                 [org.clojure/google-closure-library "0.0-20190213-2033d5d9" :scope "provided"]
                 [org.clojure/tools.cli "0.4.2"]
                 [org.clojure/tools.logging "0.5.0"]
                 [org.webjars.npm/bulma "0.7.5"]
                 [org.webjars.npm/material-icons "0.3.0"]
                 [org.webjars/webjars-locator "0.36"]
                 [ring-webjars "0.2.0"]
                 [ring/ring-core "1.7.1"]
                 [ring/ring-defaults "0.3.2"]
                 [selmer "1.12.14"]
                 [thheller/shadow-cljs "2.8.39" :scope "provided"]]

  :min-lein-version "2.0.0"
  
  :source-paths ["src/clj" "src/cljs" "src/cljc"]
  :test-paths ["test/clj"]
  :resource-paths ["resources" "target/cljsbuild"]
  :target-path "target/%s/"
  :main ^:skip-aot size-measure.core

  :plugins [[lein-shadow "0.1.5"]]
  :clean-targets ^{:protect false}
  [:target-path "target/cljsbuild"]
  :shadow-cljs
  {:nrepl {:port 7002}
   :builds
   {:app
    {:target :browser
     :output-dir "target/cljsbuild/public/js"
     :asset-path "/js"
     :modules {:app {:entries [size-measure.app]}}
     :devtools {:watch-dir "resources/public"}}
    :test
    {:target :node-test
     :output-to "target/test/test.js"
     :autorun true}}}
  
  :npm-deps [[shadow-cljs "2.8.39"]]

  :profiles
  {:uberjar {:omit-source true
             :prep-tasks ["compile" ["shadow" "release" "app"]]
             
             :aot :all
             :uberjar-name "size-measure.jar"
             :source-paths ["env/prod/clj" "env/prod/cljs"]
             :resource-paths ["env/prod/resources"]}

   :dev           [:project/dev :profiles/dev]
   :test          [:project/dev :project/test :profiles/test]

   :project/dev  {:jvm-opts ["-Dconf=dev-config.edn"]
                  :dependencies [[binaryage/devtools "0.9.10"]
                                 [cider/piggieback "0.4.1"]
                                 [pjstadig/humane-test-output "0.9.0"]
                                 [prone "2019-07-08"]
                                 [ring/ring-devel "1.7.1"]
                                 [ring/ring-mock "0.4.0"]]
                  :plugins      [[com.jakemccrary/lein-test-refresh "0.24.1"]]
                  
                  
                  :source-paths ["env/dev/clj" "env/dev/cljs" "test/cljs"]
                  :resource-paths ["env/dev/resources"]
                  :repl-options {:init-ns user}
                  :injections [(require 'pjstadig.humane-test-output)
                               (pjstadig.humane-test-output/activate!)]}
   :project/test {:jvm-opts ["-Dconf=test-config.edn"]
                  :resource-paths ["env/test/resources"]
                  
                  
                  }
   :profiles/dev {}
   :profiles/test {}})
