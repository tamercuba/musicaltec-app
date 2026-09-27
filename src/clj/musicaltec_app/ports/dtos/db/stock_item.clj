(ns musicaltec-app.ports.dtos.db.stock-item
  (:require [musicaltec-app.ports.schema :as db.schema]
            [schema.core :as s]))

(s/defschema StockItemDb
  #:stock-item{:id                                      (db.schema/unique s/Uuid :identity)
               :name                                    s/Str
               :quantity                                s/Int
               :cost                                    s/Int
               :default-price                           s/Int
               :status                                  s/Keyword
               (s/optional-key :stock-item/brand)       s/Str
               (s/optional-key :stock-item/acquired-at) s/Inst
               (s/optional-key :stock-item/serial)      s/Str
               (s/optional-key :stock-item/notes)       s/Str
               (s/optional-key :stock-item/sold-at)     s/Inst})
