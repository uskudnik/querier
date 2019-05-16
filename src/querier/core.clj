(ns querier.core
  (:gen-class)
  (:require [clojure.string :as string])
  (:require [querier.op :refer [op]])
  (:require [querier.utils :refer [valid-macro-chain? macro?]])
)

(def supported_dialects #{:postgres :mysql :sqlserver})

(defn parse-where
      "Parses where statement"
      [dialect fields query macros]
      (case query
                 {} nil
                 (string/join " " ["WHERE", (op dialect fields query macros)])
      )
)

(defn parse-limit
      "Builds limiting "
      [dialect arg]

      (case arg
            :all nil
            (case dialect
                  :sqlserver (str "TOP " arg)
                  (str "LIMIT " arg)
            )
      )
)

(defn parse-query
      "Parses specified query"
      [dialect fields query macros]
      (let [
            where_conditions (parse-where dialect fields (get query :where {}) macros)
            limit_clause (parse-limit dialect (get query :limit :all))
            ]
           (case dialect
                 :sqlserver (string/join " " (remove nil? ["SELECT", limit_clause, "* FROM data", where_conditions]))
                 (string/join " " (remove nil? ["SELECT * FROM data", where_conditions, limit_clause])))
        )
      )

(defn generate-sql
      "Generate SQL query for selected dialect"
      [dialect fields query & macros]
      (case (contains? supported_dialects dialect)
          true (case (valid-macro-chain? (first macros))
                  false (throw (Exception. "Circular macros detected"))
                  true (parse-query dialect fields query macros)
               )
          false (throw (Exception. "Unsupported SQL dialect"))
        )
      )
