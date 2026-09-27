(ns musicaltec-app.domain.models.financial-movement
  (:require [musicaltec-app.domain.models.stock :as models.stock]
            [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(def types #{:stock})
(s/defschema Type (apply s/enum types))

(def directions #{:in :out})
(s/defschema Direction (apply s/enum directions))

(def installment-statuses #{:pending :paid})
(s/defschema InstallmentStatus (apply s/enum installment-statuses))

(s/defschema Installment
  #:installment{:number                s/Int
                :due-date              s/Inst
                :amount                value/NonNegativeInt
                :status                InstallmentStatus
                (s/optional-key :responsible) s/Uuid
                (s/optional-key :paid-at)     s/Inst})

(s/defschema InstallmentInput
  #:installment{:due-date s/Inst
                :amount   value/NonNegativeInt})

(s/defschema FinancialMovementBase
  #:financial-movement{:id                        s/Uuid
                       :type                      Type
                       :direction                 Direction
                       :amount                    value/NonNegativeInt
                       :date                      s/Inst
                       (s/optional-key :counterparty) s/Str
                       (s/optional-key :description)  s/Str
                       :shared?                   s/Bool
                       :installments              [Installment]})

(s/defschema FinancialMovement
  (s/conditional
   #(= (:financial-movement/type %) :stock)
   (merge FinancialMovementBase models.stock/StockFields)))

(s/defschema FinancialMovementInput
  #:financial-movement{:type                         Type
                       :direction                    Direction
                       :date                         s/Inst
                       (s/optional-key :counterparty) s/Str
                       (s/optional-key :description)  s/Str
                       :shared?                      s/Bool
                       :items                        [models.stock/StockLine]
                       (s/optional-key :installments) [InstallmentInput]})

(s/defschema PaginatedFinancialMovement
  {:items       [FinancialMovement]
   :query       s/Str
   :page        s/Int
   :total-pages s/Int
   :total       s/Int
   :per-page    s/Int})
