(ns querier.macro-chains-test
    (:require [clojure.test :refer :all]
      [querier.core :refer :all]
      [querier.utils :refer :all]
      )
    )



(def cyclic-macros {
                    "is_good" [:and [:macro "is_decent"] [:> [:field 4] 18]]
                    "is_decent" [:and [:macro "is_good"] [:< [:field 5] 5]]
                    "is_deep" [:and [:or [:macro "is_deep_1"] [:macro "is_deep_2"]] [:< [:field 5] 5]]
                    "is_deeper" [:and
                                 [:or
                                  [:and
                                   [:macro "is_deeper_1"] [:macro "is_deeper_2"] ]
                                  [:macro "is_deep_2"]]
                                 [:< [:field 5] 5]]
                    })
(def acyclic-macros {
                     "is_good" [:and [:> [:field 4] 30] [:> [:field 4] 18]]
                     "is_decent" [:and [:macro "is_good"] [:< [:field 5] 5]]
                     "is_deep" [:and [:or [:macro "is_deep_1"] [:macro "is_deep_2"]] [:< [:field 5] 5]]
                     "is_deeper" [:and
                                  [:or
                                   [:and
                                    [:macro "is_deeper_1"] [:macro "is_deeper_2"] ]
                                   [:macro "is_deep_2"]]
                                  [:< [:field 5] 5]]
                     })

(deftest validate-macro-circular-detection
         (testing "macro"
                  (testing "is macro true"
                           (is (= true (macro? [:macro "bzeee"]))))
                  (testing "is macro false"
                           (is (= false (macro? [:maco "bzeee"]))))
                  )
         (testing "macroval"
                  (testing "returns macro value"
                           (is (= "bzeee" (macroval [:macro "bzeee"]))))
                  )
         (testing "macro-chain"
                  (is (= #{"is_deeper_1" "is_deeper_2" "is_deep_2"} (macro-chain (get cyclic-macros "is_deeper"))))
                  )
         (testing "valid-macro-chain?"
                  (is (=
                        false
                        (valid-macro-chain? cyclic-macros)
                        ))
                  (is (=
                        true
                        (valid-macro-chain? acyclic-macros)
                        ))
                  )
         )