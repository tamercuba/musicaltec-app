(ns musicaltec-app.domain.models.stock
  (:require [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(s/defschema StockLine
  #:stock-line{:stock-item-id s/Uuid
               :quantity      value/NonNegativeInt
               :unit-price    value/NonNegativeInt})

(s/defschema StockFields
  #:financial-movement{:items [StockLine]})
