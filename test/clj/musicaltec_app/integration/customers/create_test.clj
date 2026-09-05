(ns musicaltec-app.integration.customers.create-test
  (:require [state-flow.cljtest :refer [defflow]]
            [state-flow.core :refer [flow]]
            [musicaltec-app.aux.customers :as aux.customers]
            [musicaltec-app.aux.http :as http]
            [musicaltec-app.aux.system :as aux.system]))

(defflow create
  {:init   (constantly (aux.system/new-context))
   :cleanup aux.system/close!}

  (flow "authenticate"
    (http/login! aux.system/password)
    (http/expect {:status 200} :login))

  (flow "creates a customer"
    (http/request! :created :post "/api/customers"
                   {:body (aux.customers/->create-dto)})
    (http/expect {:status 201 :body {:name "João da Silva"}} :created))

  (flow "rejects a duplicate tax id"
    (http/request! :dup-tax :post "/api/customers"
                   {:body (aux.customers/->create-dto :name "Another Customer")})
    (http/expect {:status 409} :dup-tax))

  (flow "rejects a duplicate email"
    (http/request! :dup-email :post "/api/customers"
                   {:body (aux.customers/->create-dto :name "Another"
                                                      :tax-id "529.982.247-25")})
    (http/expect {:status 409} :dup-email)))
