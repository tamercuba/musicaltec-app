(ns musicaltec-app.ports.dtos.db.financial-movement
  (:require [musicaltec-app.ports.schema :as db.schema]
            [schema.core :as s]))

(s/defschema StockLineDb
  #:stock-line{:stock-item-id s/Uuid
               :quantity      s/Int
               :unit-price    s/Int})

(s/defschema InstallmentDb
  #:installment{:number                              s/Int
                :due-date                            s/Inst
                :amount                              s/Int
                :status                              s/Keyword
                (s/optional-key :installment/responsible) s/Uuid
                (s/optional-key :installment/paid-at)     s/Inst})

(s/defschema FinancialMovementDb
  #:financial-movement{:id            (db.schema/unique s/Uuid :identity)
                       :type          s/Keyword
                       :direction     s/Keyword
                       :amount        s/Int
                       :date          s/Inst
                       (s/optional-key :financial-movement/counterparty) s/Str
                       (s/optional-key :financial-movement/description)  s/Str
                       :shared?       s/Bool
                       :items         (db.schema/component [StockLineDb])
                       :installments  (db.schema/component [InstallmentDb])})
