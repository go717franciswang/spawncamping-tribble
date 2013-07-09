(ns http-delayed-job.core
  (:require [clojure.pprint :as pp]
            [monger.core :as mg]
            [monger.collection :as mc]
            monger.joda-time
            [clj-time.core :as ct]
            [clj-http.client :as http]
            [clojure.java.io :as io])
  (:use ring.middleware.params
        ring.middleware.multipart-params)
  (:import [com.mongodb MongoOptions ServerAddress DB WriteConcern]
           [org.bson.types ObjectId]
           [java.net URI]))

(def config (delay (io/load-file (io/.getFile (resource "config.clj"))))

(mg/connect!)
(mg/set-db! (mg/get-db "monger_test"))

(def proxy-to-host "http://ubuntuserver")

(defn handler [req]
  {:status 200
   :headers {"Content-Type" "text/html"}
   :body "Hello World from Ring"})

(defn proxy-request [request-id]
  (println "Querying request..")
  (let [request (mc/find-by-id "requests" {:_id request-id})
        query-string (:query-string request)
        url (str (:server-name request) (:uri request)
                 (when query-string (str "?" query-string))) ]
    (println "Sending request..")
    (mc/update "requests"
               {:_id request-id
                :status "running"
                :updated (ct/now)})
    (let [http-req {:method (:method request)
                   :url url
                   :headers (:headers request)
                   :body (:body-bytes request)
                   :follow-redirects true
                   :throw-exceptions false
                   :as :stream}]
      (pp/pprint http-req)
      (let [response (http/request http-req)]
        (println "Got response: ")
        (pp/pprint response)))))

(defn store-request [request]
  (let [request-id (ObjectId.)]
    (mc/insert "requests"
            {:_id request-id
             :remote-addr (:remote-addr request)
             :method (:request-method request)
             :query-string (:query-string request)
             :uri (:uri request)
             :server-name (:server-name request)
             :headers (dissoc (:headers request) "host" "content-length")
             :body-bytes (if-let [len (get-in request [:headers "content-length"])]
                           (slurp-binary (:body request) (Integer/parseInt len)))
             :created (ct/now)
             :updated (ct/now)
             :status "scheduled"}
               WriteConcern/JOURNAL_SAFE)
    (def request-id (agent 0))
    (send request-id proxy-request)))

(defn wrap-spy [handler]
  (fn [request]
    (println "------------------------")
    (println "Incoming Request:")
    (pp/pprint request)
    (store-request request)
    (let [response (handler request)]
      (println "Outgoing Response Map:")
      (pp/pprint response)
      (println "------------------------")
      response)))

(def app
  (-> handler
    wrap-spy))
