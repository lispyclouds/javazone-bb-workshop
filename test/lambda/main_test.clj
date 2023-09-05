(ns lambda.main-test
  (:require [clojure.test :refer [deftest testing is]]
            [lambda.main :as m]))

(deftest clean-test
  (testing "can clean correctly"
    (let [evt {:event {:log "{\"timestamp\":\"2019-09-07T-15:50+00\",\"level\":\"WARN\",\"message\":\"This email: foo@bar.com totally needs to be removed.\"}"}}]
     (is (= {:event {:log "{\"timestamp\":\"2019-09-07T-15:50+00\",\"level\":\"WARN\",\"message\":\"This email: <REDACTED> totally needs to be removed.\"}"}}
            (m/handler evt {}))))))
