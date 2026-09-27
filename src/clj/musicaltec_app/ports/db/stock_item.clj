(ns musicaltec-app.ports.db.stock-item
  (:require [musicaltec-app.domain.models.stock-item :as models.stock-item]
            [schema.core :as s]))

(s/defprotocol StockItemRepository
  (insert! :- models.stock-item/StockItem
    [this
     item :- models.stock-item/StockItem])
  (update! :- models.stock-item/StockItem
    [this
     item :- models.stock-item/StockItem])
  (delete! :- s/Any
    [this
     id :- s/Uuid])
  (get! :- models.stock-item/StockItem
    [this
     id :- s/Uuid])
  (find-all :- [models.stock-item/StockItem]
    [this]))
