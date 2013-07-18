(ns http-delayed-job.db
  (:require [monger.core :as mg]
            [monger.collection :as mc]
            [monger.query :as mq]
            monger.joda-time
            [http-delayed-job.load-config :refer :all]
            [clj-time.core :as ct]
            [clojure.pprint :as pp])
  (:import [org.bson.types ObjectId]
           [com.mongodb MongoOptions ServerAddress DB WriteConcern]))

(mg/connect!)
(mg/set-db! (mg/get-db (:database (get-config))))

(defn slurp-binary [^java.io.InputStream is len]
  (with-open [rdr is]
    (let [buf (byte-array len)]
      (.read rdr buf)
      buf)))

(defn store [request]
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
    request-id))

(defn retrieve [request-id]
  (mc/find-one-as-map "requests" {:_id request-id}))

(defn retrieve-recent [n]
  (mq/with-collection "requests"
    (mq/find {})
    (mq/fields [:_id :created :updated :status :uri :query-string :ftp-path])
    (mq/sort {:created -1})
    (mq/limit n)))

(defn update [request-id changes]
  (let [changes (assoc changes :updated (ct/now))]
    (mc/update "requests" {:_id request-id} {:$set changes})))
