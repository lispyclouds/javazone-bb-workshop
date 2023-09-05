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
  [event _context]
  (let [log (get-in event [:event :log])
        parsed (json/parse-string log true)
        msg (get parsed :message)
        cleaned (clean-all msg)
        parsed (assoc parsed :message cleaned)
        clean-str (json/generate-string parsed)]
    (assoc-in event [:event :log] clean-str)))

(defn -main
  [& _]
  (runtime/init handler))

(comment
  (clean "this email: foo@bar.com" (get pii-patterns 0))

  (handler (json/parse-string (slurp "resources/event.json") true)
           {}))
