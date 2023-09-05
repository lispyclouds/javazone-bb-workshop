(ns lambda.main
  (:require
   [cheshire.core :as json]
   [clojure.string :as str]
   [lambda.runtime :as runtime]))

(def pii-patterns
  [#"\S+@\S+"])

(defn clean
  [s pattern]
  (str/replace s pattern "<REDACTED>"))

(defn clean-all
  [s]
  (reduce clean s pii-patterns))

(defn handler
  [{:keys [n1 n2]} _context]
  {:sum (+ n1 n2)})

(defn -main
  [& _]
  (runtime/init handler))

(comment
  (clean "this email: foo@bar.com" (get pii-patterns 0))

  (handler (json/parse-string (slurp "resources/event.json") true)
           {}))
