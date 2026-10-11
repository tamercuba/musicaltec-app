(ns musicaltec-app.ports.dtos.db.stock-item
  (:require [schema.core :as s]))

(s/defschema StockItemDb
  {:id            s/Str
   :name          s/Str
   :quantity      s/Int
   :cost          s/Int
   :default-price s/Int
   :available     s/Int
   :status        s/Str
   :created-at    s/Str
   :updated-at    s/Str
   (s/optional-key :brand)       (s/maybe s/Str)
   (s/optional-key :acquired-at) (s/maybe s/Str)
   (s/optional-key :serial)      (s/maybe s/Str)
   (s/optional-key :notes)       (s/maybe s/Str)})
