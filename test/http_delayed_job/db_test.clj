(ns http-delayed-job.db-test
  (:require [clojure.test :refer :all]
            [http-delayed-job.db :refer :all]
            [clojure.pprint :as pp]))

(def test-request {:remote-addr "192.0.0.1"
                  :request-method "post"
                  :query-string "x=1&y=2"
                  :uri "/path"
                  :server-name "test-server"
                  :headers {:z "123"}
                  :body nil})

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
    (update request-id {:status "running"})
    (let [request (retrieve request-id)]
      (is (= "running" (:status request))))))

        
