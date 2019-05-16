(ns querier.utils
    (:require [clojure.set :as set])
    (:require [querier.kahn :refer [kahn-sort]])
)


(defn almost-flatten
  ;; Courtesy of https://stackoverflow.com/a/5236875
  [x]
  (filter #(and (sequential? %) (not-any? sequential? %))
          (rest (tree-seq #(and (sequential? %) (some sequential? %)) seq x))))


(defn macro?
  ([v]
    (if (= (first v) :macro)
      true
      false)))


(defn macroval
  ([v]
    (if (vector? v)
      (if (= (first v) :macro)
        (second v)
        nil)
      nil)
    ))


(defn macro-chain
  ([macro]
    (into #{} (flatten (map macroval (filter macro? (almost-flatten macro)))))
  ))


(defn valid-macro-chain?
  ([macros]
    (let [
          chains (zipmap (keys macros) (map macro-chain (vals macros)))
          ]
         (if (nil? (kahn-sort chains)) false true))
    ))
