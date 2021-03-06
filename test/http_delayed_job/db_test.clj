(ns http-delayed-job.db-test
  (:require [clojure.test :refer :all]
            [http-delayed-job.load-config :refer :all]
            [http-delayed-job.db :refer :all]
            [clojure.pprint :as pp]
            [monger.collection :as mc])
  (:use [clj-time.coerce :only [to-long]]))

(def test-request {:remote-addr "192.0.0.1"
                  :request-method "post"
                  :query-string "x=1&y=2"
                  :uri "/path"
                  :server-name "test-server"
                  :headers {:z "123"}
                  :body nil})

(defn clean-up-requests [f]
  (mc/remove "requests")
  (f))

(use-fixtures :each clean-up-requests)

(deftest loading-db-name
  (is (= "delayed_job_test" (:database (get-config)))))

(deftest store-a-request
  (let [request-id (store test-request)]
    (is (instance? org.bson.types.ObjectId request-id))))

(deftest retrieve-a-request 
  (let [request-id (store test-request)
        request (retrieve request-id)]
    (is (= request-id (:_id request)))
    (is (= "192.0.0.1" (:remote-addr request)))))

(deftest update-a-request
  (let [request-id (store test-request)]
    ; sleep for 5 milliseconds to make sure updated is different from created
    (Thread/sleep 5)
    (update request-id {:status "running"})
    (let [request (retrieve request-id)]
      (is (not (= (:created request) (:updated request))))
      (is (= "192.0.0.1" (:remote-addr request)))
      (is (= "running" (:status request))))))

(deftest retrieve-recent-requests
  (dotimes [_ 5] (store test-request))
  (let [requests (retrieve-recent 3)
        top-req (first requests)
        bottom-req (last requests)]
    (is (= (count requests) 3))
    (is (> (to-long (:created top-req)) 
           (to-long (:created bottom-req))))))
        
