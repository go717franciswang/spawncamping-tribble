(ns http-delayed-job.db-test
  (:require [clojure.test :refer :all]
            [http-delayed-job.db :refer :all]
            [clojure.pprint :as pp]
            [monger.collection :as mc]))

(def test-request {:remote-addr "192.0.0.1"
                  :request-method "post"
                  :query-string "x=1&y=2"
                  :uri "/path"
                  :server-name "test-server"
                  :headers {:z "123"}
                  :body nil})

(mc/remove "requests")

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

        
