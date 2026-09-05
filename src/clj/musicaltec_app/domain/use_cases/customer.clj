(ns musicaltec-app.domain.use-cases.customer
  (:require
   [musicaltec-app.domain.logic.customer :as logic.customer]
   [musicaltec-app.domain.models.customer :as models.customer]
   [musicaltec-app.domain.models.pagination :as models.pagination]
   [musicaltec-app.ports.db.customer :as ports.db.customer]
   [musicaltec-app.ports.system :as ports.system]
   [schema.core :as s]))

(s/defn create :- models.customer/Customer
  [customer :- models.customer/Customer
   {:keys [db/customer-repo]} :- ports.system/Adapters]
  (ports.db.customer/insert! customer-repo customer))

(s/defn update :- models.customer/Customer
  [customer :- models.customer/Customer
   {:keys [db/customer-repo]} :- ports.system/Adapters]
  (ports.db.customer/update! customer-repo customer))

(s/defn delete :- s/Any
  [id :- s/Uuid
   {:keys [db/customer-repo]} :- ports.system/Adapters]
  (ports.db.customer/delete! customer-repo id))

(s/defn get :- models.customer/Customer
  [id :- s/Uuid
   {:keys [db/customer-repo]} :- ports.system/Adapters]
  (ports.db.customer/get! customer-repo id))

(s/defn list :- models.customer/PaginatedCustomer
  [query                      :- models.pagination/Query
   {:keys [db/customer-repo]} :- ports.system/Adapters]
  (-> customer-repo
      ports.db.customer/find-all
      (logic.customer/filter-matching query)
      logic.customer/sort-by-name
      (logic.customer/->paginated query)))
