(ns musicaltec-app.domain.models.stock
  (:require [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(s/defschema StockLine
  #:stock-line{:stock-item-id s/Uuid
               :quantity      value/NonNegativeInt
               :unit-price    value/NonNegativeInt})

(s/defschema NewItemLine
  #:stock-line{:name                                s/Str
               (s/optional-key :stock-line/brand)   s/Str
               :quantity                            value/NonNegativeInt
               :unit-price                          value/NonNegativeInt
               (s/optional-key :stock-line/default-price) value/NonNegativeInt
               (s/optional-key :stock-line/serial)  s/Str
               (s/optional-key :stock-line/notes)   s/Str})

(s/defschema StockLineInput
  (s/conditional
   #(contains? % :stock-line/stock-item-id) StockLine
   #(contains? % :stock-line/name)          NewItemLine))

(s/defschema StockFields
  #:financial-movement{:items [StockLine]})
