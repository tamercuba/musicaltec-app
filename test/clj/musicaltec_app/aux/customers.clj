(ns musicaltec-app.aux.customers
  (:require [state-flow.core :refer [flow]]
            [musicaltec-app.aux.http :as http]
            [musicaltec-app.aux.system :as aux.system]))

(def create-dto
  {:name   "João da Silva"
   :phones ["11 99999-0001"]
   :email  "joao@example.com"
   :kind   "person"
   :tax-id "111.444.777-35"})

(defn ->create-dto [& {:as overrides}]
  (merge create-dto overrides))

(defn seed!
  "Flow: authenticates and creates a customer, storing the response in `:created`."
  []
  (flow "authenticate and create a customer"
    (http/login! aux.system/password)
    (http/expect {:status 200} :login)
    (http/request! :created :post "/api/customers" {:body (->create-dto)})
    (http/expect {:status 201 :body {:name "João da Silva"}} :created)))

(defn customer-url [ctx]
  (str "/api/customers/" (get-in ctx [:created :body :id])))
