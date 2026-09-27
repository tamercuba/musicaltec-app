(ns musicaltec-app.domain.use-cases.stock-item
  (:require [musicaltec-app.domain.logic.stock-item :as logic.stock-item]
            [musicaltec-app.domain.models.pagination :as models.pagination]
            [musicaltec-app.domain.models.stock-item :as models.stock-item]
            [musicaltec-app.ports.db.stock-item :as ports.db.stock-item]
            [musicaltec-app.ports.system :as ports.system]
            [schema.core :as s]))

(s/defn create :- models.stock-item/StockItem
  [item :- models.stock-item/StockItem
   {:keys [db/stock-item-repo]} :- ports.system/Adapters]
  (ports.db.stock-item/insert! stock-item-repo item))

(s/defn update :- models.stock-item/StockItem
  [item :- models.stock-item/StockItem
   {:keys [db/stock-item-repo]} :- ports.system/Adapters]
  (ports.db.stock-item/update! stock-item-repo item))

(s/defn delete :- s/Any
  [id :- s/Uuid
   {:keys [db/stock-item-repo]} :- ports.system/Adapters]
  (ports.db.stock-item/delete! stock-item-repo id))

(s/defn get :- models.stock-item/StockItem
  [id :- s/Uuid
   {:keys [db/stock-item-repo]} :- ports.system/Adapters]
  (ports.db.stock-item/get! stock-item-repo id))

(s/defn list :- models.stock-item/PaginatedStockItem
  [query                      :- models.pagination/Query
   {:keys [db/stock-item-repo]} :- ports.system/Adapters]
  (-> stock-item-repo
      ports.db.stock-item/find-all
      (logic.stock-item/filter-matching query)
      logic.stock-item/sort-by-name
      (logic.stock-item/->paginated query)))
