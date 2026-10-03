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
  [{:financial-movement/keys [direction date installments-count down-payment]
    :as input} :- models.financial-movement/FinancialMovementInput
   {:keys [db/stock-item-repo db/financial-movement-repo]} :- ports.system/Adapters]
  (let [raw-items  (:financial-movement/items input)
        resolved   (mapv (fn [line]
                           (if-let [sid (:stock-line/stock-item-id line)]
                             line
                             (let [created (ports.db.stock-item/insert!
                                            stock-item-repo
                                            (logic.stock/new-item->stock-item line))]
                               #:stock-line{:stock-item-id (:stock-item/id created)
                                            :quantity      (:stock-line/quantity line)
                                            :unit-price    (:stock-line/unit-price line)})))
                         raw-items)
        total      (logic.stock/items-total resolved)
        _          (when-not (logic.financial-movement/down-payment-valid? down-payment total)
                     (errors/fail! :financial-movement/down-payment-invalid))
        installments (logic.financial-movement/->installments date installments-count down-payment total)
        movement   (logic.financial-movement/->new (assoc input :financial-movement/items resolved) total installments (random-uuid))]
    (when (= direction :in)
      (doseq [{:stock-line/keys [stock-item-id quantity]} resolved]
        (let [item (ports.db.stock-item/get! stock-item-repo stock-item-id)]
          (when-not (logic.stock/stock-available? item quantity)
            (errors/fail! :financial-movement/stock-unavailable)))))
    (doseq [line raw-items
            :when (contains? line :stock-line/stock-item-id)]
      (let [{:stock-line/keys [stock-item-id quantity unit-price]} line
            item    (ports.db.stock-item/get! stock-item-repo stock-item-id)
            updated (logic.stock/apply-stock-effect item quantity unit-price direction)]
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
