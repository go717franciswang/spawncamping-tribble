(ns http-delayed-job.core-test
  (:require [clojure.test :refer :all]
            [http-delayed-job.load-config :refer :all]
            [http-delayed-job.core :refer :all]
            [clojure.data.json :as json]
            clojure.java.io))

(defn clean-up-tmp-dir [f]
  (let [ftp-dir (clojure.java.io/file (:ftp-dir (get-config)))
        files (rest (file-seq ftp-dir))]
    (doseq [file files]
      (clojure.java.io/delete-file file)))
  (f))

(use-fixtures :each clean-up-tmp-dir)

(defn substring? [needle haystack]
  (let [l (count needle)
        r (range (inc (- (count haystack) l)))]
    (boolean (some #(= % needle) (map #(subs haystack % (+ % l)) r)))))

(deftest substring-exists
  (is (= true (substring? "ab" "abc")))
  (is (= true (substring? "ab" "abc")))
  (is (= true (substring? "abc" "abc")))
  (is (= false (substring? "d" "abc"))))

(deftest respond-json-recent-requests
  (let [resp (app {:uri "/requests"})
        part-resp (json/write-str ["request-id" "created" "updated" "status" "ftp-path" "uri" "query-string"])]
    (is (substring? part-resp (:body resp)))))

(deftest respond-json-ok
  (let [resp (app {})]
    (is (= 200 (:status resp)))
    (is (= "application/json" (get-in resp [:headers "Content-Type"])))))

(deftest download-csv-as-file
  (let [request {:method :post
                 :uri ""
                 :query-string ""
                 :body-bytes nil}
        downloaded-file (download-as-csv request)]
    (is (not (= nil downloaded-file)))))
