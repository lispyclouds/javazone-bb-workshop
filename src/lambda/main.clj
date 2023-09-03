(ns lambda.main
  (:require
   [lambda.runtime :as runtime]))

(defn handler
  [{:keys [n1 n2]} _context]
  {:sum (+ n1 n2)})

(defn -main
  [& _]
  (runtime/init handler))
