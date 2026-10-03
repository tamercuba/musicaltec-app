(ns musicaltec-app.unit.domain.logic.financial-movement-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.domain.logic.financial-movement :as logic.financial-movement]))

(deftest ->installments-a-vista
  (let [xs (logic.financial-movement/->installments #inst "2026-01-15" nil nil 100000)]
    (is (= 1 (count xs)))
    (is (= 1 (:installment/number (first xs))))
    (is (= :pending (:installment/status (first xs))))
    (is (= 100000 (:installment/amount (first xs))))))

(deftest ->installments-parcelado
  (let [xs (logic.financial-movement/->installments #inst "2026-01-15" 3 nil 100000)]
    (is (= 3 (count xs)))
    (is (= 33334 (:installment/amount (first xs))))
    (is (= 33333 (:installment/amount (second xs))))
    (is (every? #(= :pending (:installment/status %)) xs))))

(deftest ->installments-with-down-payment
  (let [xs (logic.financial-movement/->installments #inst "2026-01-15" 4 250000 1000000)]
    (is (= 4 (count xs)))
    (is (= :paid (:installment/status (first xs))))
    (is (= 250000 (:installment/amount (first xs))))
    (is (some? (:installment/paid-at (first xs))))
    (is (= 250000 (:installment/amount (second xs))))))

(deftest down-payment-valid
  (is (true? (logic.financial-movement/down-payment-valid? nil 100)))
  (is (true? (logic.financial-movement/down-payment-valid? 50 100)))
  (is (false? (logic.financial-movement/down-payment-valid? 100 100)))
  (is (false? (logic.financial-movement/down-payment-valid? 150 100))))
