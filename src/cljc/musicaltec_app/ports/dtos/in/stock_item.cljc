(ns musicaltec-app.ports.dtos.in.stock-item
  (:require [clojure.string :as str]
            [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(def ^:private StockItemFields
  {:name                            (s/constrained s/Str (complement str/blank?))
   (s/optional-key :brand)          s/Str
   (s/optional-key :quantity)       value/NonNegativeInt
   (s/optional-key :cost)           value/NonNegativeInt
   (s/optional-key :default-price)  value/NonNegativeInt
   (s/optional-key :serial)         s/Str
   (s/optional-key :notes)          s/Str})

(s/defschema CreateStockItemIn
  StockItemFields)

(s/defschema UpdateStockItemIn
  (merge StockItemFields {:id s/Uuid}))

(s/defschema ListStockItemsIn
  {(s/optional-key :q)        s/Str
   (s/optional-key :page)     s/Str
   (s/optional-key :per-page) s/Str
   (s/optional-key :available) s/Str})
