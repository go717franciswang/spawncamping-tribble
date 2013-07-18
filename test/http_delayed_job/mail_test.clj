(ns http-delayed-job.mail-test
  (:require [clojure.test :refer :all]
            [http-delayed-job.mail :refer :all]
            [clojure.pprint :as pp]))

(deftest send-mail-to-user
  (let [email "bpo_data@localhost"
        rs (send-mail email {})]
    (is (= "message sent" (:message rs)))))
