(ns musicaltec-app.ports.dtos.out.financial-movement
  (:require [schema.core :as s]))

(s/defschema StockLineOut
  {:stock-item-id s/Str
   :quantity      s/Int
   :unit-price    s/Int})

(s/defschema InstallmentOut
  {:number                        s/Int
   :due-date                      s/Inst
   :amount                        s/Int
   :status                        (s/enum :pending :paid)
   (s/optional-key :responsible)  s/Str
   (s/optional-key :paid-at)      s/Inst})

(s/defschema FinancialMovementOut
  {:id                          s/Str
   :type                        (s/enum :stock)
   :direction                   (s/enum :in :out)
   :amount                      s/Int
   :date                        s/Inst
   (s/optional-key :counterparty) s/Str
   (s/optional-key :description)  s/Str
   :shared?                     s/Bool
   (s/optional-key :items)      [StockLineOut]
   :installments                [InstallmentOut]})

(s/defschema ListFinancialMovementsOut
  {:items       [FinancialMovementOut]
   :query       s/Str
   :page        s/Int
   :total-pages s/Int
   :total       s/Int
   :per-page    s/Int})
