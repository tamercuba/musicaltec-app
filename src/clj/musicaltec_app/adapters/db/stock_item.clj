(ns musicaltec-app.adapters.db.stock-item
  (:require [musicaltec-app.adapters.db.core :as db.core]
            [musicaltec-app.adapters.db.schema :as db.schema]
            [musicaltec-app.domain.errors :as errors]
            [musicaltec-app.domain.mappers.stock-item :as mappers.stock-item]
            [musicaltec-app.ports.db.stock-item :as ports.db.stock-item]
            [musicaltec-app.ports.dtos.db.stock-item :as dto.db.stock-item]
            [schema.core :as s]))

(def schema
  (db.schema/db-schema->datomic dto.db.stock-item/StockItemDb))

(s/defrecord DatomicStockItemRepository [conn :- s/Any]
  ports.db.stock-item/StockItemRepository
  (insert! [_ item]
    (db.core/transact! conn [(mappers.stock-item/model->db item)] (fn [_] (errors/fail! :resource/conflict)))
    item)

  (update! [_ item]
    (let [id (:stock-item/id item)]
      (when-not (:stock-item/id (db.core/pull conn '[:stock-item/id] [:stock-item/id id]))
        (errors/fail! :resource/not-found))
      (db.core/transact! conn [(mappers.stock-item/model->db item)] (fn [_] (errors/fail! :resource/conflict)))
      item))

  (delete! [_ id]
    (db.core/delete-by-id conn [:stock-item/id id]))

  (get! [_ id]
    (db.core/get-by-id conn [:stock-item/id id] :stock-item/id mappers.stock-item/db->model))

  (find-all [_]
    (db.core/find-all conn :stock-item/id mappers.stock-item/db->model)))

(s/defn ->repository :- (s/protocol ports.db.stock-item/StockItemRepository)
  [conn :- s/Any]
  (map->DatomicStockItemRepository {:conn conn}))
