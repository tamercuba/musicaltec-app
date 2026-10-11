(ns musicaltec-app.adapters.db.sql.customer
  (:require [honey.sql :as honey]
            [musicaltec-app.adapters.db.sql.core :as sql]
            [musicaltec-app.adapters.db.sql.errors]
            [musicaltec-app.domain.errors :as errors]
            [musicaltec-app.domain.mappers.customer :as mappers.customer]
            [musicaltec-app.ports.db.customer :as ports.db.customer]
            [musicaltec-app.ports.errors.db.core :as db-errors]
            [schema.core :as s]))

(s/defrecord SqlCustomerRepository [ds :- s/Any]
  ports.db.customer/CustomerRepository
  (insert! [_ customer]
    (try
      (sql/with-transaction ds
        (fn [tx]
          (sql/execute! tx
                        (honey/format
                         {:insert-into :customers
                          :values      [(mappers.customer/model->db customer :active (sql/now))]}))
          (when (-> customer :customer/phones seq)
            (sql/execute! tx
                          (honey/format
                           {:insert-into :customer-phones
                            :values      (mappers.customer/model->db-phones customer)})))))
      (catch Exception e
        (db-errors/fail! e :customer)))
    customer)

  (update! [_ customer]
    (try
      (sql/with-transaction ds
        (fn [tx]
          (let [result (sql/execute! tx
                                     (honey/format
                                      {:update :customers
                                       :set    (-> (mappers.customer/model->db customer :active (sql/now))
                                                   (dissoc :id :created-at :status))
                                       :where  [:= :id (-> customer :customer/id str)]}))]
            (when (zero? (sql/update-count result))
              (errors/fail! :resource/not-found))
            (sql/execute! tx
                          (honey/format
                           {:delete-from :customer-phones
                            :where       [:= :customer-id (-> customer :customer/id str)]}))
            (when (-> customer :customer/phones seq)
              (sql/execute! tx
                            (honey/format
                             {:insert-into :customer-phones
                              :values      (mappers.customer/model->db-phones customer)}))))))
      (catch Exception e
        (db-errors/fail! e :customer)))
    customer)

  (delete! [_ id]
    (sql/execute! ds
                  (honey/format
                   {:update :customers
                    :set    {:status "inactive" :updated-at (sql/now)}
                    :where  [:= :id (str id)]}))
    nil)

  (get! [_ id]
    (let [id-str   (str id)
          customer (sql/query-one! ds
                                   (honey/format
                                    {:select [:*]
                                     :from   [:customers]
                                     :where  [:and [:= :id id-str] [:= :status "active"]]}))]
      (if-not customer
        (errors/fail! :resource/not-found)
        (mappers.customer/db->model
         customer
         (sql/query! ds
                     (honey/format
                      {:select   [:*]
                       :from     [:customer-phones]
                       :where    [:= :customer-id id-str]
                       :order-by [:position]}))))))

  (find-all [_]
    (let [rows (sql/query! ds
                           (honey/format
                            {:select [:*]
                             :from   [:customers]
                             :where  [:= :status "active"]}))]
      (if (empty? rows)
        []
        (let [ids    (map :id rows)
              phones (->> (sql/query! ds
                                      (honey/format
                                       {:select   [:*]
                                        :from     [:customer-phones]
                                        :where    [:in :customer-id ids]
                                        :order-by [:customer-id :position]}))
                          (group-by :customer-id))]
          (mapv (fn [row]
                  (mappers.customer/db->model row (get phones (:id row))))
                rows))))))

(s/defn ->repository :- (s/protocol ports.db.customer/CustomerRepository)
  [ds :- s/Any]
  (map->SqlCustomerRepository {:ds ds}))
