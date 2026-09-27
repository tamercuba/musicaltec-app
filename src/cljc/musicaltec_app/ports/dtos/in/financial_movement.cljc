(ns musicaltec-app.ports.dtos.in.financial-movement
  (:require [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(s/defschema StockLineIn
  {:stock-item-id s/Uuid
   :quantity      value/NonNegativeInt
   :unit-price    value/NonNegativeInt})

(s/defschema InstallmentIn
  {:due-date s/Inst
   :amount   value/NonNegativeInt})

(s/defschema CreateFinancialMovementIn
  {:type                       (s/enum :stock)
   :direction                  (s/enum :in :out)
   :date                       s/Inst
   (s/optional-key :counterparty) s/Str
   (s/optional-key :description)  s/Str
   (s/optional-key :shared?)      s/Bool
   (s/optional-key :installments) [InstallmentIn]
   :items                      [StockLineIn]})

(s/defschema ListFinancialMovementsIn
  {(s/optional-key :q)        s/Str
   (s/optional-key :page)     s/Str
   (s/optional-key :per-page) s/Str})
