(ns http-delayed-job.mail
  (:require [clojure.pprint :as pp])
  (:import java.io.StringWriter)
  (:use [postal.core :only [send-message]]))

(defn send-mail [email request]
  (let [host-name (.getHostName (java.net.InetAddress/getLocalHost))
        from (str "delayed-job-noreply@" host-name)
        w (StringWriter.)
        msg (do 
              (pp/pprint request w)
              (.toString w))]
    (send-message {:from from
                   :to [email]
                   :subject "Delayed job completed"
                   :body msg})))
