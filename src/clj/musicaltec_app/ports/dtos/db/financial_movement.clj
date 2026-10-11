(ns musicaltec-app.ports.dtos.db.financial-movement
  (:require [schema.core :as s]))

(s/defschema FinancialMovementDb
  {:id           s/Str
   :type         s/Str
   :direction    s/Str
   :amount       s/Int
   :date         s/Str
   :shared       s/Int
   :status       s/Str
   :counterparty (s/maybe s/Str)
   :description  (s/maybe s/Str)
   :created-at   s/Str
   :updated-at   s/Str})

(s/defschema StockLineDb
  {:movement-id   s/Str
   :position      s/Int
   :stock-item-id s/Str
   :quantity      s/Int
   :unit-price    s/Int})

(s/defschema InstallmentDb
  {:movement-id s/Str
   :number      s/Int
   :due-date    s/Str
   :amount      s/Int
   :status      s/Str
   :responsible (s/maybe s/Str)
   :paid-at     (s/maybe s/Str)})
