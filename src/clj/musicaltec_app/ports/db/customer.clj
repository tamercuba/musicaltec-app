(ns musicaltec-app.ports.db.customer
  (:require [musicaltec-app.domain.models.customer :as models.customer]
            [schema.core :as s]))

(s/defprotocol CustomerRepository
  (insert! :- models.customer/Customer
    [this
     customer :- models.customer/Customer])
  (update! :- models.customer/Customer
    [this
     customer :- models.customer/Customer])
  (delete! :- s/Any
    [this
     id :- s/Uuid])
  (get! :- models.customer/Customer
    [this
     id :- s/Uuid])
  (find-all :- [models.customer/Customer]
    [this]))
