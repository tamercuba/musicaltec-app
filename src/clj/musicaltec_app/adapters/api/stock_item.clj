(ns musicaltec-app.adapters.api.stock-item
  (:require [musicaltec-app.domain.mappers.pagination :as mappers.pagination]
            [musicaltec-app.domain.mappers.stock-item :as mappers.stock-item]
            [musicaltec-app.domain.use-cases.stock-item :as use-cases.stock-item]
            [musicaltec-app.ports.dtos.in.stock-item :as dtos.in.stock-item]
            [schema.core :as s]))

(defn- ok         [body] {:status 200 :body body})
(defn- created    [body] {:status 201 :body body})
(defn- no-content [_]    {:status 204})

(defn create-handler
  [{:keys [dto adapters]}]
  (-> dto
      mappers.stock-item/dto->model
      (use-cases.stock-item/create adapters)
      mappers.stock-item/model->dto
      created))

(defn list-handler
  [{:keys [dto adapters]}]
  (-> dto
      mappers.stock-item/query->model
      (use-cases.stock-item/list adapters)
      (mappers.pagination/paginated->dto mappers.stock-item/model->dto)
      ok))

(defn get-handler
  [{:keys [dto adapters]}]
  (-> (:id dto)
      (use-cases.stock-item/get adapters)
      mappers.stock-item/model->dto
      ok))

(defn update-handler
  [{:keys [dto adapters]}]
  (-> dto
      mappers.stock-item/update-dto->model
      (use-cases.stock-item/update adapters)
      mappers.stock-item/model->dto
      ok))

(defn delete-handler
  [{:keys [dto adapters]}]
  (-> dto
      :id
      (use-cases.stock-item/delete adapters)
      no-content))

(def routes
  [["/stock-items"
    {:post   {:handler    create-handler
              :parameters {:body dtos.in.stock-item/CreateStockItemIn}}
     :get    {:handler    list-handler
              :parameters {:query dtos.in.stock-item/ListStockItemsIn}}}]
   ["/stock-items/:id"
    {:get    {:handler    get-handler
              :parameters {:path {:id s/Uuid}}}
     :put    {:handler    update-handler
              :parameters {:path {:id s/Uuid}
                           :body dtos.in.stock-item/CreateStockItemIn}}
     :delete {:handler    delete-handler
              :parameters {:path {:id s/Uuid}}}}]])
