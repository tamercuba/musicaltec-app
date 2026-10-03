(ns musicaltec-app.adapters.api.customer
  (:require [schema.core :as s]
            [musicaltec-app.domain.mappers.customer :as mappers.customer]
            [musicaltec-app.domain.mappers.pagination :as mappers.pagination]
            [musicaltec-app.domain.use-cases.customer :as use-cases.customer]
            [musicaltec-app.ports.dtos.in.customer :as dtos.in.customer]))

(defn- ok         [body] {:status 200 :body body})
(defn- created    [body] {:status 201 :body body})
(defn- no-content [_]    {:status 204})

(defn create-handler
  [{:keys [dto adapters]}]
  (-> dto
      mappers.customer/dto->model
      (use-cases.customer/create adapters)
      mappers.customer/model->dto
      created))

(defn list-handler
  [{:keys [dto adapters]}]
  (-> dto
      mappers.pagination/dto->model
      (use-cases.customer/list adapters)
      (mappers.pagination/model->dto mappers.customer/model->dto)
      ok))

(defn get-handler
  [{:keys [dto adapters]}]
  (-> (:id dto)
      (use-cases.customer/get adapters)
      mappers.customer/model->dto
      ok))

(s/defn update-handler
  [{:keys [dto adapters]}]
  (-> dto
      mappers.customer/update-dto->model
      (use-cases.customer/update adapters)
      mappers.customer/model->dto
      ok))

(defn delete-handler
  [{:keys [dto adapters]}]
  (-> dto
      :id
      (use-cases.customer/delete adapters)
      no-content))

(def routes
  [["/customers"
    {:post   {:handler    create-handler
              :parameters {:body dtos.in.customer/CreateCustomerIn}}
     :get    {:handler    list-handler
              :parameters {:query dtos.in.customer/ListCustomersIn}}}]
   ["/customers/:id"
    {:get    {:handler    get-handler
              :parameters {:path {:id s/Uuid}}}
     :put    {:handler    update-handler
              :parameters {:path {:id s/Uuid}
                           :body dtos.in.customer/CreateCustomerIn}}
     :delete {:handler    delete-handler
              :parameters {:path {:id s/Uuid}}}}]])
