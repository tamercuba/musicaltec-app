(ns musicaltec-app.unit.domain.logic.financial-movement-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.domain.logic.financial-movement :as logic.financial-movement]))

(deftest ->installments-single-a-vista
  (let [xs (logic.financial-movement/->installments #inst "2026-01-15" nil 100000)]
    (is (= 1 (count xs)))
    (is (= 1 (:installment/number (first xs))))
    (is (= :pending (:installment/status (first xs))))
    (is (= 100000 (:installment/amount (first xs))))))

(deftest ->installments-parcelado
  (let [xs (logic.financial-movement/->installments
            #inst "2026-01-15"
            [{:installment/due-date #inst "2026-01-15" :installment/amount 50000}
             {:installment/due-date #inst "2026-02-15" :installment/amount 50000}]
            100000)]
    (is (= 2 (count xs)))
    (is (= 1 (:installment/number (first xs))))
    (is (= 2 (:installment/number (second xs))))))

(deftest installments-valid
  (is (true? (logic.financial-movement/installments-valid? nil 100)))
  (is (true? (logic.financial-movement/installments-valid? [{:installment/due-date #inst "2026-01-15" :installment/amount 100}] 100)))
  (is (false? (logic.financial-movement/installments-valid? [{:installment/due-date #inst "2026-01-15" :installment/amount 50}] 100))))
