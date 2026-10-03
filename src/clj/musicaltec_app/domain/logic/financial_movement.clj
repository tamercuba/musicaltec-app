(ns musicaltec-app.domain.logic.financial-movement
  (:require [musicaltec-app.domain.logic.pagination :as logic.pagination]
            [musicaltec-app.domain.models.financial-movement :as models.financial-movement]
            [musicaltec-app.domain.models.pagination :as models.pagination]
            [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(defmulti ->new
  "Builds a `FinancialMovement` from its input, total amount and installments."
  (fn [input & _] (:financial-movement/type input)))

(s/defn down-payment-valid? :- s/Bool
  [down-payment :- (s/maybe value/NonNegativeInt)
   total        :- s/Int]
  (or (nil? down-payment)
      (not (pos? down-payment))
      (< down-payment total)))

(defn- split-amounts [total n]
  (let [base  (quot total n)
        extra (rem total n)]
    (cons (+ base extra) (repeat (dec n) base))))

(defn- ->installment [number date amount status paid-at]
  (cond-> #:installment{:number   number
                        :due-date date
                        :amount   amount
                        :status   status}
    paid-at (assoc :installment/paid-at paid-at)))

(s/defn ->installments :- [models.financial-movement/Installment]
  [date         :- s/Inst
   count        :- (s/maybe s/Int)
   down-payment :- (s/maybe value/NonNegativeInt)
   total        :- s/Int]
  (let [n (max 1 (or count 1))]
    (if (and down-payment (pos? down-payment))
      (let [remaining (- total down-payment)
            rest-n    (max 1 (dec n))
            amounts   (split-amounts remaining rest-n)]
        (mapv (fn [i amt]
                (->installment i date amt (if (= i 1) :paid :pending) (when (= i 1) date)))
              (range 1 (inc n))
              (cons down-payment amounts)))
      (mapv (fn [i amt] (->installment i date amt :pending nil))
            (range 1 (inc n))
            (split-amounts total n)))))

(s/defn sort-by-date-desc :- [models.financial-movement/FinancialMovement]
  [movements :- [models.financial-movement/FinancialMovement]]
  (sort-by (juxt :financial-movement/date :financial-movement/id)
           #(compare %2 %1)
           movements))

(s/defn ->paginated :- models.financial-movement/PaginatedFinancialMovement
  [movements :- [models.financial-movement/FinancialMovement]
   query     :- models.pagination/Query]
  (logic.pagination/->paginated movements query))
