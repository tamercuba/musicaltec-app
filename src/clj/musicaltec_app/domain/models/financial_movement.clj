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
  #:installment{:number                              s/Int
                :due-date                            s/Inst
                :amount                              value/NonNegativeInt
                :status                              InstallmentStatus
                (s/optional-key :installment/responsible) s/Uuid
                (s/optional-key :installment/paid-at)     s/Inst})

(s/defschema FinancialMovementBase
  #:financial-movement{:id            s/Uuid
                       :type          Type
                       :direction     Direction
                       :amount        value/NonNegativeInt
                       :date          s/Inst
                       (s/optional-key :financial-movement/counterparty) s/Str
                       (s/optional-key :financial-movement/description)  s/Str
                       :shared?       s/Bool
                       :installments  [Installment]})

(s/defschema FinancialMovement
  (s/conditional
   #(= (:financial-movement/type %) :stock)
   (merge FinancialMovementBase models.stock/StockFields)))

(s/defschema FinancialMovementInput
  #:financial-movement{:type       Type
                       :direction  Direction
                       :date       s/Inst
                       (s/optional-key :financial-movement/counterparty)       s/Str
                       (s/optional-key :financial-movement/description)        s/Str
                       :shared?    s/Bool
                       :items      [models.stock/StockLineInput]
                       (s/optional-key :financial-movement/installments-count) s/Int
                       (s/optional-key :financial-movement/down-payment)       value/NonNegativeInt})

(s/defschema PaginatedFinancialMovement
  {:items       [FinancialMovement]
   :query       s/Str
   :page        s/Int
   :total-pages s/Int
   :total       s/Int
   :per-page    s/Int})
