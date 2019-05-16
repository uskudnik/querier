(ns querier.op
    (:require [clojure.string :as string])
)


(defmacro evalArg
          ([dialect fields arg macros]
            `(cond
               (= (vector? ~arg) true) (op ~dialect ~fields ~arg ~macros)
               (= (number? ~arg) true) (str ~arg)
               (= (string? ~arg) true) (str "'" ~arg "'")
               (= (nil? ~arg) true) "NULL"
               )))

(defn columnQuoter
      [dialect columnName]
      (case dialect
            :mysql (str "`" columnName "`")
            (str columnName )
            ;(str "\"" columnName "\"")
            ))

(defn fieldOp
      [dialect fields opVector macros]
      (let [fieldKey (get opVector 1)]
           (columnQuoter dialect (name (get fields fieldKey))))
      )

(defmacro ltOp
          ;; arg ::= <field> | <number>
          ;[:< <arg> <arg] ; x > y
          ([dialect fields opVector macros]
            `(string/join " " [
                               (evalArg ~dialect ~fields (get ~opVector 1) ~macros)
                               "<"
                               (evalArg ~dialect ~fields (get ~opVector 2) ~macros)])
            ))

(defmacro gtOp
          ;; arg ::= <field> | <number>
          ;[:< <arg> <arg] ; x > y
          ([dialect fields opVector macros]
            `(string/join " " [
                               (evalArg ~dialect ~fields (get ~opVector 1) ~macros)
                               ">"
                               (evalArg ~dialect ~fields (get ~opVector 2) ~macros)])
            ))

(defn evalList
      [dialect fields arg]
      (str "(" (string/join ", ", arg) ")")
      )

(defn eval2Operator
      [dialect fields arg]
      (cond
        (= (nil? arg) true) "IS"
        :else "="
        ))

(defn eval2NegateOperator
      [dialect fields arg]
      (cond
        (= (nil? arg) true) "IS NOT"
        :else "<>"
        ))

(defmacro eqOp
          ;; arg ::= <field> | <number> | <string> | nil
          ;[:= <x> <y>]      ; x = y
          ;[:= <x> <y> <z>]  ; x IN (y, z)
          ([dialect fields opVector macros]
            `(case (count ~opVector)
                   3 (string/join " " [
                                       (evalArg ~dialect ~fields (get ~opVector 1) ~macros)
                                       (eval2Operator ~dialect ~fields (get ~opVector 2))
                                       (evalArg ~dialect ~fields (get ~opVector 2) ~macros)])
                   (string/join " " [
                                     (evalArg ~dialect ~fields (get ~opVector 1) ~macros),
                                     "IN"
                                     (evalList ~dialect ~fields (subvec ~opVector 2))
                                     ])
                   )))

(defmacro notEqOp
          ;; arg ::= <field> | <number> | <string> | nil
          ;[:= <x> <y>]      ; x != y
          ;[:= <x> <y> <z>]  ; x NOT IN (y, z)
          ([dialect fields opVector macros]
            `(case (count ~opVector)
                   3 (string/join " " [
                                       (evalArg ~dialect ~fields (get ~opVector 1) ~macros)
                                       (eval2NegateOperator ~dialect ~fields (get ~opVector 2))
                                       (evalArg ~dialect ~fields (get ~opVector 2) ~macros)])
                   (string/join " " [
                                     (evalArg ~dialect ~fields (get ~opVector 1) ~macros),
                                     "NOT IN"
                                     (evalList ~dialect ~fields (subvec ~opVector 2))
                                     ])
                   )))


(defmacro andOp
          ([d f v m]
            `(str "("
                  (evalArg ~d ~f (get ~v 1) ~m)
                  " AND "
                  (evalArg ~d ~f (get ~v 2) ~m)
                  ")")
            ))

(defmacro orOp
          ([d f v m]
            `(str "("
                  (evalArg ~d ~f (get ~v 1) ~m)
                  " OR "
                  (evalArg ~d ~f (get ~v 2) ~m)
                  ")" )
            ))

(defmacro notOp
          ([d f v m]
            `(str "NOT " (evalArg ~d ~f (get ~v 1) ~m))
            ))

(defmacro isEmptyOp
          ([d f v m]
            `(str
               (evalArg ~d ~f (get ~v 1) ~m)
               " IS NULL")
            ))

(defmacro notEmptyOp
          ([d f v m]
            `(str
               (evalArg ~d ~f (get ~v 1) ~m)
               " IS NOT NULL"
               )
            ))

(defmacro macroOp
          ([d f v m]
            `(op ~d ~f (get (first ~m) (get ~v 1)) ~m)
            )
          )


(defmulti op (fn [d f v m] (first v)))
(defmethod op :and ([d f v m] (andOp d f v m)))
(defmethod op :or ([d f v m] (orOp d f v m)))
(defmethod op :not ([d f v m] (notOp d f v m)))
(defmethod op :field ([d f v m] (fieldOp d f v m)))
(defmethod op :macro ([d f v m] (macroOp d f v m)))
(defmethod op :< ([d f v m] (ltOp d f v m)))
(defmethod op :> ([d f v m] (gtOp d f v m)))
(defmethod op := ([d f v m] (eqOp d f v m)))
(defmethod op :!= ([d f v m] (notEqOp d f v m)))
(defmethod op :is-empty ([d f v m] (isEmptyOp d f v m)))
(defmethod op :not-empty ([d f v m] (notEmptyOp d f v m)))