(ns musicaltec-app.adapters.db.financial-movement
  (:require [datomic.api :as d]
            [musicaltec-app.adapters.db.core :as db.core]
            [musicaltec-app.adapters.db.schema :as db.schema]
            [musicaltec-app.domain.errors :as errors]
            [musicaltec-app.domain.mappers.financial-movement :as mappers.financial-movement]
            [musicaltec-app.ports.db.financial-movement :as ports.db.financial-movement]
            [musicaltec-app.ports.dtos.db.financial-movement :as dto.db.financial-movement]
            [schema.core :as s]))

(def schema
  (db.schema/db-schema->datomic dto.db.financial-movement/FinancialMovementDb))

(def ^:private pull-pattern
  '[:financial-movement/id
    :financial-movement/type
    :financial-movement/direction
    :financial-movement/amount
    :financial-movement/date
    :financial-movement/counterparty
    :financial-movement/description
    :financial-movement/shared?
    {:financial-movement/items [*]}
    {:financial-movement/installments [*]}])

(s/defrecord DatomicFinancialMovementRepository [conn :- s/Any]
  ports.db.financial-movement/FinancialMovementRepository
  (insert! [_ movement]
    (db.core/transact! conn [(mappers.financial-movement/model->db movement)] (fn [_] (errors/fail! :resource/conflict)))
    movement)

  (get! [_ id]
    (db.core/get-by-pattern conn pull-pattern [:financial-movement/id id] :financial-movement/id mappers.financial-movement/db->model))

  (find-all [_]
    (->> (d/q '[:find (pull ?e [:financial-movement/id
                                :financial-movement/type
                                :financial-movement/direction
                                :financial-movement/amount
                                :financial-movement/date
                                :financial-movement/counterparty
                                :financial-movement/description
                                :financial-movement/shared?
                                {:financial-movement/items [*]}
                                {:financial-movement/installments [*]}])
                :in $ :where [?e :financial-movement/id]]
              (d/db conn))
         (map first)
         (map mappers.financial-movement/db->model)
         (vec))))

(s/defn ->repository :- (s/protocol ports.db.financial-movement/FinancialMovementRepository)
  [conn :- s/Any]
  (map->DatomicFinancialMovementRepository {:conn conn}))
