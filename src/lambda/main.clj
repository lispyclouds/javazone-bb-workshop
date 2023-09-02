(ns lambda.main
  (:require
   [lambda.runtime :as runtime]))

(defn -main
  [& _]
  (runtime/init (fn [request]
                  (prn request)
                  request)))
