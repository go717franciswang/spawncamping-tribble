(ns http-delayed-job.core
  (:require [clojure.pprint :as pp]
            [http-delayed-job.load-config :refer :all]
            [http-delayed-job.db :as db]
            [clj-http.client :as client]
            [clojure.data.json :as json])
  (:use ring.middleware.params
        ring.middleware.multipart-params
        [clojure.java.io :only [output-stream]]))

(def proxy-to-host "http://127.0.0.1")

(defn handler [req]
  {:status 200
   :headers {"Content-Type" "application/json"}})

(defn download-as-csv [request]
  (let [method (keyword (:method request))
        query (:query-string request)
        url-prefix (str proxy-to-host (:uri request))
        url (if query (str url-prefix "?" query) url-prefix)
        body (:body-bytes request)
        args {:method method
              :url url
              :body body
              :socket-timeout 21600000
              :conn-timeout 21600000
              :as :byte-array}
        resp (client/request args)]
    (if (= 200 (:status resp))
      (let [ftp-dir (:ftp-dir (get-config))
            filename (str (java.util.UUID/randomUUID) ".csv")
            filepath (str ftp-dir "/" filename)]
        (with-open [o (output-stream filepath)]
          (.write o (:body resp)))
        filename)
      nil)))

(defn proxy-request [request-id]
  (db/update request-id {:status "running"})
  (let [request (db/retrieve request-id)
        filename (download-as-csv request)
        ftp-path (str (:ftp-dir-path (get-config)) "/" filename)]
    (db/update request-id {:status "completed" :ftp-path ftp-path})))

(defn wrap-spy [handler]
  (fn [request]
    (println "------------------------")
    (println "Incoming Request:")
    (pp/pprint request)
    (let [request-id (db/store request)
          request-id-agent (agent request-id)
          body (json/write-str [["status" "request-id" "ftp-dir"] 
                                ["scheduled" (str request-id) (:ftp-dir-path (get-config))]])
          response (assoc (handler request) :body body)]
      (send request-id-agent proxy-request)
      (println "Outgoing Response Map:")
      (pp/pprint response)
      (println "------------------------")
      response)))

(def app
  (-> handler
    wrap-spy))
