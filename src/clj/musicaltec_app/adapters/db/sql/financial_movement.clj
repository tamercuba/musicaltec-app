(ns musicaltec-app.adapters.db.sql.financial-movement
  (:require [honey.sql :as honey]
            [musicaltec-app.adapters.db.sql.core :as sql]
            [musicaltec-app.domain.errors :as errors]
            [musicaltec-app.domain.mappers.financial-movement :as mappers.financial-movement]
            [musicaltec-app.ports.db.financial-movement :as ports.db.financial-movement]
            [schema.core :as s]))

(s/defrecord SqlFinancialMovementRepository [ds :- s/Any]
  ports.db.financial-movement/FinancialMovementRepository
  (insert! [_ movement]
    (sql/with-transaction ds
      (fn [tx]
        (sql/execute! tx
                      (honey/format
                       {:insert-into :financial-movements
                        :values      [(mappers.financial-movement/model->db movement :active (sql/now))]}))
        (when (-> movement :financial-movement/items seq)
          (sql/execute! tx
                        (honey/format
                         {:insert-into :financial-movement-items
                          :values      (mappers.financial-movement/model->db-items movement)})))
        (when (-> movement :financial-movement/installments seq)
          (sql/execute! tx
                        (honey/format
                         {:insert-into :installments
                          :values      (mappers.financial-movement/model->db-installments movement)})))))
    movement)

  (get! [_ id]
    (let [id-str   (str id)
          movement (sql/query-one! ds
                                   (honey/format
                                    {:select [:*]
                                     :from   [:financial-movements]
                                     :where  [:and
                                              [:= :id id-str]
                                              [:= :status "active"]]}))]
      (if movement
        (mappers.financial-movement/db->model
         movement
         (sql/query! ds
                     (honey/format
                      {:select   [:*]
                       :from     [:financial-movement-items]
                       :where    [:= :movement-id id-str]
                       :order-by [:position]}))
         (sql/query! ds
                     (honey/format
                      {:select   [:*]
                       :from     [:installments]
                       :where    [:= :movement-id id-str]
                       :order-by [:number]})))
        (errors/fail! :resource/not-found))))

  (find-all [_]
    (let [rows (sql/query! ds
                           (honey/format
                            {:select [:*]
                             :from   [:financial-movements]
                             :where  [:= :status "active"]}))]
      (if (empty? rows)
        []
        (let [ids   (map :id rows)
              items (->> (sql/query! ds
                                     (honey/format
                                      {:select   [:*]
                                       :from     [:financial-movement-items]
                                       :where    [:in :movement-id ids]
                                       :order-by [:movement-id :position]}))
                         (group-by :movement-id))
              insts (->> (sql/query! ds
                                     (honey/format
                                      {:select   [:*]
                                       :from     [:installments]
                                       :where    [:in :movement-id ids]
                                       :order-by [:movement-id :number]}))
                         (group-by :movement-id))]
          (mapv (fn [row]
                  (mappers.financial-movement/db->model
                   row
                   (get items (:id row))
                   (get insts (:id row))))
                rows))))))

(s/defn ->repository :- (s/protocol ports.db.financial-movement/FinancialMovementRepository)
  [ds :- s/Any]
  (map->SqlFinancialMovementRepository {:ds ds}))
