(defproject http-delayed-job "0.1.0-SNAPSHOT"
  :description "An Ring tutorial project"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.1.8"]
                 [ring/ring-jetty-adapter "1.1.8"]
                 [com.novemberain/monger "1.6.0"]
                 [cheshire "5.1.1"]
                 [clj-time "0.5.1"]
                 [clj-http "0.7.4"]
                 [com.draines/postal "1.10.3"]
                 [org.clojure/data.json "0.2.2"]]
  :plugins [[lein-ring "0.8.3"]]
  :profiles {:test {:resource-paths ["resource-test"]}
             :dev {:resource-paths ["resource-test"]}
             :prod {:resource-paths ["resource-prod"]}}
  :jvm-opts ["-Xmx500m"]
  :ring {:handler http-delayed-job.core/app})
