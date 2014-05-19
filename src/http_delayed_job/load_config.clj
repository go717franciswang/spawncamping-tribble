(ns http-delayed-job.load-config 
  (:require [clojure.java.io :as io]))

(def config 
  (delay 
    (do
      (println "loading configuration..")
      (load-file (io/.getFile (io/resource "config.clj"))))))
(defn get-config [] @(force config))
