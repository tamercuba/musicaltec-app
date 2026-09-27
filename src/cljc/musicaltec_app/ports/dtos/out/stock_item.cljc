(ns musicaltec-app.ports.dtos.out.stock-item
  (:require [schema.core :as s]))

(s/defschema StockItemOut
  {:id                       s/Str
   :name                     s/Str
   (s/optional-key :brand)   s/Str
   :quantity                 s/Int
   :cost                     s/Int
   :default-price            s/Int
   (s/optional-key :acquired-at) s/Inst
   :status                   (s/enum :in-stock :sold)
   (s/optional-key :serial)  s/Str
   (s/optional-key :notes)   s/Str
   (s/optional-key :sold-at) s/Inst})

(s/defschema ListStockItemsOut
  {:items       [StockItemOut]
   :query       s/Str
   :page        s/Int
   :total-pages s/Int
   :total       s/Int
   :per-page    s/Int})
