(defn fact
  [n]
  (reduce * 1 (range 1 (inc n))))

(when (= *file* (System/getProperty "babashka.file"))
  (println (fact 5)))

(comment
  (fact 5))
