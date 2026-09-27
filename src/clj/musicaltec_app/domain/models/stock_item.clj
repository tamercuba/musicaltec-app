(ns musicaltec-app.domain.models.stock-item
  (:require [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(def statuses #{:in-stock :sold})

(s/defschema Status (apply s/enum statuses))

(s/defschema StockItem
  {:stock-item/id                       s/Uuid
   :stock-item/name                     s/Str
   (s/optional-key :stock-item/brand)   s/Str
   :stock-item/quantity                 value/NonNegativeInt
   :stock-item/cost                     value/NonNegativeInt
   :stock-item/default-price            value/NonNegativeInt
   (s/optional-key :stock-item/acquired-at) s/Inst
   :stock-item/status                   Status
   (s/optional-key :stock-item/serial)  s/Str
   (s/optional-key :stock-item/notes)   s/Str
   (s/optional-key :stock-item/sold-at) s/Inst})

(s/defschema PaginatedStockItem
  {:items       [StockItem]
   :query       s/Str
   :page        s/Int
   :total-pages s/Int
   :total       s/Int
   :per-page    s/Int})
