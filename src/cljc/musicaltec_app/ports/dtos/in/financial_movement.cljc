(ns musicaltec-app.ports.dtos.in.financial-movement
  (:require [clojure.string :as str]
            [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(s/defschema ExistingStockLineIn
  {:stock-item-id s/Uuid
   :quantity      value/NonNegativeInt
   :unit-price    value/NonNegativeInt})

(s/defschema NewStockLineIn
  {:name                           (s/constrained s/Str (complement str/blank?))
   (s/optional-key :brand)         s/Str
   :quantity                       value/NonNegativeInt
   :unit-price                     value/NonNegativeInt
   (s/optional-key :default-price) value/NonNegativeInt
   (s/optional-key :serial)        s/Str
   (s/optional-key :notes)         s/Str})

(s/defschema StockLineIn
  (s/conditional
   #(contains? % :stock-item-id) ExistingStockLineIn
   #(contains? % :name)          NewStockLineIn))

(s/defschema CreateFinancialMovementIn
  {:type                            (s/enum :stock)
   :direction                       (s/enum :in :out)
   :date                            s/Inst
   (s/optional-key :counterparty)   s/Str
   (s/optional-key :description)    s/Str
   (s/optional-key :shared?)        s/Bool
   (s/optional-key :installments-count) s/Int
   (s/optional-key :down-payment)   value/NonNegativeInt
   :items                           [StockLineIn]})

(s/defschema ListFinancialMovementsIn
  {(s/optional-key :q)        s/Str
   (s/optional-key :page)     s/Str
   (s/optional-key :per-page) s/Str})
