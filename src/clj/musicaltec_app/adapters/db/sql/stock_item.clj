(ns musicaltec-app.adapters.db.sql.stock-item
  (:require [honey.sql :as honey]
            [musicaltec-app.adapters.db.sql.core :as sql]
            [musicaltec-app.domain.errors :as errors]
            [musicaltec-app.domain.mappers.stock-item :as mappers.stock-item]
            [musicaltec-app.ports.db.stock-item :as ports.db.stock-item]
            [schema.core :as s]))

(s/defrecord SqlStockItemRepository [ds :- s/Any]
  ports.db.stock-item/StockItemRepository
  (insert! [_ item]
    (let [db-dto (mappers.stock-item/model->db item :active (sql/now))]
      (sql/execute! ds
                    (honey/format
                     {:insert-into :stock-items
                      :values      [db-dto]}))
      item))

  (update! [_ item]
    (let [{:keys [id] :as db-dto} (mappers.stock-item/model->db item :active (sql/now))
          result (sql/execute! ds
                               (honey/format
                                {:update :stock-items
                                 :set    (dissoc db-dto :id :created-at :status)
                                 :where  [:= :id id]}))]
      (when (zero? (sql/update-count result))
        (errors/fail! :resource/not-found))
      item))

  (delete! [_ id]
    (sql/execute! ds
                  (honey/format
                   {:update :stock-items
                    :set    {:status "inactive" :updated-at (sql/now)}
                    :where  [:= :id (str id)]}))
    nil)

  (get! [_ id]
    (let [row (sql/query-one! ds
                              (honey/format
                               {:select [:*]
                                :from   [:stock-items]
                                :where  [:and [:= :id (str id)] [:= :status "active"]]}))]
      (if-not row
        (errors/fail! :resource/not-found)
        (mappers.stock-item/db->model row))))

  (find-all [_]
    (->> (sql/query! ds
                     (honey/format
                      {:select [:*]
                       :from   [:stock-items]
                       :where  [:= :status "active"]}))
         (mapv mappers.stock-item/db->model))))

(s/defn ->repository :- (s/protocol ports.db.stock-item/StockItemRepository)
  [ds :- s/Any]
  (map->SqlStockItemRepository {:ds ds}))
