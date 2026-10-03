(ns musicaltec-app.adapters.api.financial-movement
  (:require [musicaltec-app.domain.mappers.financial-movement :as mappers.financial-movement]
            [musicaltec-app.domain.mappers.pagination :as mappers.pagination]
            [musicaltec-app.domain.use-cases.financial-movement :as use-cases.financial-movement]
            [musicaltec-app.ports.dtos.in.financial-movement :as dtos.in.financial-movement]
            [schema.core :as s]))

(defn- ok      [body] {:status 200 :body body})
(defn- created [body] {:status 201 :body body})

(defn register-handler
  [{:keys [dto adapters]}]
  (-> dto
      mappers.financial-movement/dto->model
      (use-cases.financial-movement/register adapters)
      mappers.financial-movement/model->dto
      created))

(defn list-handler
  [{:keys [dto adapters]}]
  (-> dto
      mappers.pagination/query->model
      (use-cases.financial-movement/list adapters)
      (mappers.pagination/paginated->dto mappers.financial-movement/model->dto)
      ok))

(defn get-handler
  [{:keys [dto adapters]}]
  (-> (:id dto)
      (use-cases.financial-movement/get adapters)
      mappers.financial-movement/model->dto
      ok))

(def routes
  [["/financial-movements"
    {:post {:handler    register-handler
            :parameters {:body dtos.in.financial-movement/CreateFinancialMovementIn}}
     :get  {:handler    list-handler
            :parameters {:query dtos.in.financial-movement/ListFinancialMovementsIn}}}]
   ["/financial-movements/:id"
    {:get {:handler     get-handler
           :parameters  {:path {:id s/Uuid}}}}]])
