(ns querier.core-test
  (:require [clojure.test :refer :all]
            [querier.core :refer :all]
            [querier.utils :refer :all]
  )
)


(deftest validate-dialects
         (testing "Accepts dialect"
                  (testing "MySQL"
                           (is (= "SELECT * FROM data" (generate-sql :mysql "fieeeeldz" "query"))))
                  (testing "PostgreSQL"
                           (is (= "SELECT * FROM data" (generate-sql :postgres "fieeeeldz" "query"))))
                  (testing "SQL Server"
                           (is (= "SELECT * FROM data" (generate-sql :sqlserver "fieeeeldz" "query"))))
                  (testing "unsupported"
                           (is (thrown-with-msg? Exception #"Unsupported SQL dialect" (generate-sql :foobar "fieeeeldz" "query"))))
                  ))

(def limit_only_query {:limit 20})

(deftest validate-limit-clause
         (testing "Limits query for"
                  (testing "MySQL"
                           (is (= "SELECT * FROM data LIMIT 20" (generate-sql :mysql "fieeeeldz" limit_only_query))))
                  (testing "PostgreSQL"
                           (is (= "SELECT * FROM data LIMIT 20" (generate-sql :postgres "fieeeeldz" limit_only_query))))
                  (testing "SQL Server"
                           (is (= "SELECT TOP 20 * FROM data" (generate-sql :sqlserver "fieeeeldz" limit_only_query))))
                  ))


(def test_fields {1 :id
                  2 :name
                  3 :date_joined
                  4 :age})


(deftest validate-where-query-postgresql
         (testing "Where conditions for PostgreSQL"
                  (testing "{:where [:= [:field 3] nil]}"
                           (is (= "SELECT * FROM data WHERE \"date_joined\" IS NULL"
                                  (generate-sql :postgres test_fields {:where [:= [:field 3] nil]}))))
                  (testing "{:where [:> [:field 4] 35]"
                           (is (= "SELECT * FROM data WHERE \"age\" > 35"
                                  (generate-sql :postgres test_fields {:where [:> [:field 4] 35]}))))
                  (testing "{:where [:and [:< [:field 1] 5] [:= [:field 2] \"joe\"]]}"
                           (is (= "SELECT * FROM data WHERE (\"id\" < 5 AND \"name\" = 'joe')"
                                  (generate-sql :postgres test_fields {:where [:and [:< [:field 1] 5] [:= [:field 2] "joe"]]}))))
                  (testing "{:where [:or [:!= [:field 3] \"2015-11-01\"] [:= [:field 1] 456]]}"
                           (is (= "SELECT * FROM data WHERE (\"date_joined\" <> '2015-11-01' OR \"id\" = 456)"
                                  (generate-sql :postgres test_fields {:where [:or [:!= [:field 3] "2015-11-01"] [:= [:field 1] 456]]}))))
                  (testing "{:where [:and [:!= [:field 3] nil] [:or [\">\" [:field 4] 25] [:= [:field 2] \"Jerry\"]]]})"
                           (is (= "SELECT * FROM data WHERE (\"date_joined\" IS NOT NULL AND (\"age\" > 25 OR \"name\" = 'Jerry'))"
                                  (generate-sql :postgres test_fields {:where [:and [:!= [:field 3] nil] [:or [:> [:field 4] 25] [:= [:field 2] "Jerry"]]]}))))
                  (testing "{:where [:= [:field 4] 25 26 27]}"
                           (is (= "SELECT * FROM data WHERE \"age\" IN (25, 26, 27)"
                                  (generate-sql :postgres test_fields {:where [:= [:field 4] 25 26 27]}))))
                  (testing "{:where [:= [:field 2] \"cam\"]}"
                           (is (= "SELECT * FROM data WHERE \"name\" = 'cam'"
                                  (generate-sql :postgres test_fields {:where [:= [:field 2] "cam"]}))))
                  (testing "{:where [:is-empty [:field 2]]}"
                           (is (= "SELECT * FROM data WHERE \"name\" IS NULL"
                                  (generate-sql :postgres test_fields {:where [:is-empty [:field 2]]}))))
                  (testing "{:where [:not-empty [:field 2]]}"
                           (is (= "SELECT * FROM data WHERE \"name\" IS NOT NULL"
                                  (generate-sql :postgres test_fields {:where [:not-empty [:field 2]]}))))
                  ))

(deftest validate-where-query-mysql
         (testing "Where conditions for MySQL"
                  (testing "{:where [:= [:field 3] nil]}"
                           (is (= "SELECT * FROM data WHERE `name` = 'cam' LIMIT 10"
                                  (generate-sql :mysql test_fields {:where [:= [:field 2] "cam"], :limit 10})
                                  )))
                  ))

(deftest validate-macros
    (testing "Basic macro support"
     (is (= "SELECT * FROM data WHERE (`id` < 5 AND `name` = 'joe')"
            (generate-sql
                :mysql
                test_fields
                {:where [:and [:< [:field 1] 5] [:macro "is_joe"]]}
                {
                 "is_joe" [:= [:field 2] "joe"]
                 "is_foo" [:= [:field 2] "foo"]
                }
            )
     ))
    )
    (testing "Nested macro support"
     (is (= "SELECT * FROM data WHERE (`id` < 5 AND (`age` > 18 AND `name` = 'joe'))"
            (generate-sql
                :mysql
                test_fields
                {:where [:and [:< [:field 1] 5] [:macro "is_adult_joe"]]}
                {
                 "is_joe" [:= [:field 2] "joe"]
                 "is_adult" [:> [:field 4] 18]
                 "is_adult_joe" [:and [:macro "is_adult"] [:macro "is_joe"]]
                }
            )
     ))
    )
    (testing "Circular macros detection"
             (is (thrown-with-msg? Exception #"Circular macros detected"
                                   (generate-sql
                                     :mysql
                                     test_fields
                                     {:where [:and [:< [:field 1] 5] [:macro "is_good"]]}
                                     {
                                      "is_good" [:and [:macro "is_decent"] [:> [:field 4] 18]]
                                      "is_decent" [:and [:macro "is_good"] [:< [:field 5] 5]]
                                     })))
    )
)
