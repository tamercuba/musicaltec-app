(ns musicaltec-app.domain.use-cases.financial-movement
  (:require [musicaltec-app.domain.errors :as errors]
            [musicaltec-app.domain.logic.financial-movement :as logic.financial-movement]
            [musicaltec-app.domain.logic.stock :as logic.stock]
            [musicaltec-app.domain.models.financial-movement :as models.financial-movement]
            [musicaltec-app.domain.models.pagination :as models.pagination]
            [musicaltec-app.ports.db.financial-movement :as ports.db.financial-movement]
            [musicaltec-app.ports.db.stock-item :as ports.db.stock-item]
            [musicaltec-app.ports.system :as ports.system]
            [schema.core :as s]))

(s/defn register :- models.financial-movement/FinancialMovement
  [{:financial-movement/keys [items direction date installments]
    :as input} :- models.financial-movement/FinancialMovementInput
   {:keys [db/stock-item-repo db/financial-movement-repo]} :- ports.system/Adapters]
  (let [total              (logic.stock/items-total items)
        _                  (when-not (logic.financial-movement/installments-valid? installments total)
                             (errors/fail! :financial-movement/installments-mismatch))
        valid-installments (logic.financial-movement/->installments date installments total)
        movement           (logic.financial-movement/->new input total valid-installments (random-uuid))]
    (when (= direction :in)
      (doseq [{:stock-line/keys [stock-item-id quantity]} items]
        (let [item (ports.db.stock-item/get! stock-item-repo stock-item-id)]
          (when-not (logic.stock/stock-available? item quantity)
            (errors/fail! :financial-movement/stock-unavailable)))))
    (doseq [{:stock-line/keys [stock-item-id quantity]} items]
      (let [item    (ports.db.stock-item/get! stock-item-repo stock-item-id)
            updated (logic.stock/apply-stock-effect item quantity direction date)]
        (ports.db.stock-item/update! stock-item-repo updated)))
    (ports.db.financial-movement/insert! financial-movement-repo movement)))

(s/defn get :- models.financial-movement/FinancialMovement
  [id :- s/Uuid
   {:keys [db/financial-movement-repo]} :- ports.system/Adapters]
  (-> financial-movement-repo
      (ports.db.financial-movement/get! id)))

(s/defn list :- models.financial-movement/PaginatedFinancialMovement
  [query :- models.pagination/Query
   {:keys [db/financial-movement-repo]} :- ports.system/Adapters]
  (-> financial-movement-repo
      ports.db.financial-movement/find-all
      logic.financial-movement/sort-by-date-desc
      (logic.financial-movement/->paginated query)))
