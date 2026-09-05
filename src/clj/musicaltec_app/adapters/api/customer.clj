(ns musicaltec-app.adapters.api.customer
  (:require [schema.core :as s]
            [musicaltec-app.domain.mappers.customer :as mappers.customer]
            [musicaltec-app.domain.mappers.pagination :as mappers.pagination]
            [musicaltec-app.domain.use-cases.customer :as use-cases.customer]
            [musicaltec-app.ports.dtos.in.customer :as dtos.in.customer]))

(defn- ok [body]      {:status 200 :body body})
(defn- created [body] {:status 201 :body body})
(defn- no-content []  {:status 204})

(s/defn create-handler :- s/Any
  [{:keys [dto adapters]} :- s/Any]
  (-> dto
      mappers.customer/dto->model
      (use-cases.customer/create adapters)
      mappers.customer/model->dto
      created))

(s/defn list-handler :- s/Any
  [{:keys [dto adapters]} :- s/Any]
  (-> dto
      mappers.pagination/dto->model
      (use-cases.customer/list adapters)
      (mappers.pagination/model->dto mappers.customer/model->dto)
      ok))

(s/defn get-handler :- s/Any
  [{:keys [dto adapters]} :- s/Any]
  (-> (:id dto)
      (use-cases.customer/get adapters)
      mappers.customer/model->dto
      ok))

(s/defn update-handler :- s/Any
  [{:keys [dto adapters]} :- s/Any]
  (-> dto
      mappers.customer/dto->model
      (use-cases.customer/update adapters)
      mappers.customer/model->dto
      ok))

(s/defn delete-handler :- s/Any
  [{:keys [dto adapters]} :- s/Any]
  (use-cases.customer/delete (:id dto) adapters)
  (no-content))

(def routes
  [["/customers"
    {:post {:handler create-handler
            :parameters {:body dtos.in.customer/CreateCustomerIn}}
     :get  {:handler list-handler
            :parameters {:query dtos.in.customer/ListCustomersIn}}}]
   ["/customers/:id"
    {:get    {:handler get-handler
              :parameters {:path {:id s/Uuid}}}
     :put    {:handler update-handler
              :parameters {:path {:id s/Uuid}
                           :body dtos.in.customer/CreateCustomerIn}}
     :delete {:handler delete-handler
              :parameters {:path {:id s/Uuid}}}}]])
