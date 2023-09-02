(ns lambda.main
  (:require
   [lambda.runtime :as runtime]))

(defn process
  [{:keys [n1 n2]}]
  {:sum (+ n1 n2)})

(defn -main
  [& _]
  (runtime/init process))
