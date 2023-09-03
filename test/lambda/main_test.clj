(ns lambda.main-test
  (:require [clojure.test :refer [deftest testing is]]
            [lambda.main :as m]))

(deftest process-test
  (testing "can process correctly"
    (is (= {:sum 9} (m/process {:n1 4 :n2 5} {})))))
