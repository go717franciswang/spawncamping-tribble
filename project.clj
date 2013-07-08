(defproject ring-tutorial "0.1.0-SNAPSHOT"
  :description "An Ring tutorial project"
  :url "http://example.com/FIXME"
  :license {:name "Eclipse Public License"
            :url "http://www.eclipse.org/legal/epl-v10.html"}
  :dependencies [[org.clojure/clojure "1.5.1"]
                 [ring/ring-core "1.1.8"]
                 [ring/ring-jetty-adapter "1.1.8"]
                 [com.novemberain/monger "1.5.0"]
                 [cheshire "5.1.1"]
                 [clj-time "0.5.1"]
                 [clj-http "0.7.4"]]
  :plugins [[lein-ring "0.8.3"]]
  :ring {:handler ring-tutorial.core/app})
