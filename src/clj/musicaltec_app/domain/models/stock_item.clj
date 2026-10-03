(ns musicaltec-app.domain.models.stock-item
  (:require [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(s/defschema StockItem
  #:stock-item{:id                                      s/Uuid
               :name                                    s/Str
               (s/optional-key :stock-item/brand)       s/Str
               :quantity                                value/NonNegativeInt
               :cost                                    value/NonNegativeInt
               :default-price                           value/NonNegativeInt
               (s/optional-key :stock-item/acquired-at) s/Inst
               :available?                              s/Bool
               (s/optional-key :stock-item/serial)      s/Str
               (s/optional-key :stock-item/notes)       s/Str})

(s/defschema PaginatedStockItem
  {:items       [StockItem]
   :query       s/Str
   :page        s/Int
   :total-pages s/Int
   :total       s/Int
   :per-page    s/Int})
