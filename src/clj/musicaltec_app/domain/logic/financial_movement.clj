(ns musicaltec-app.domain.logic.financial-movement
  (:require [musicaltec-app.domain.logic.pagination :as logic.pagination]
            [musicaltec-app.domain.models.financial-movement :as models.financial-movement]
            [musicaltec-app.domain.models.pagination :as models.pagination]
            [schema.core :as s]))

(defmulti ->new
  "Builds a `FinancialMovement` from its input, total amount and installments."
  (fn [input & _] (:financial-movement/type input)))

(s/defn installments-valid? :- s/Bool
  [input :- (s/maybe [models.financial-movement/InstallmentInput])
   total :- s/Int]
  (if (seq input)
    (= total (reduce + 0 (map :installment/amount input)))
    true))

(s/defn ->installments :- [models.financial-movement/Installment]
  [date  :- s/Inst
   input :- (s/maybe [models.financial-movement/InstallmentInput])
   total :- s/Int]
  (if (seq input)
    (->> input
         (map-indexed (fn [i {:installment/keys [due-date amount]}]
                        #:installment{:number   (inc i)
                                      :due-date due-date
                                      :amount   amount
                                      :status   :pending}))
         vec)
    [#:installment{:number   1
                   :due-date date
                   :amount   total
                   :status   :pending}]))

(s/defn sort-by-date-desc :- [models.financial-movement/FinancialMovement]
  [movements :- [models.financial-movement/FinancialMovement]]
  (sort-by (juxt :financial-movement/date :financial-movement/id)
           #(compare %2 %1)
           movements))

(s/defn ->paginated :- models.financial-movement/PaginatedFinancialMovement
  [movements :- [models.financial-movement/FinancialMovement]
   query     :- models.pagination/Query]
  (logic.pagination/->paginated movements query))

(defmulti ->new :financial-movement/type)
