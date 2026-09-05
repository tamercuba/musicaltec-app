(ns musicaltec-app.integration.customers.update-test
  (:require [state-flow.cljtest :refer [defflow]]
            [state-flow.core :refer [flow]]
            [musicaltec-app.aux.customers :as aux.customers]
            [musicaltec-app.aux.http :as http]
            [musicaltec-app.aux.system :as aux.system]))

(defflow update
  {:init   (constantly (aux.system/new-context))
   :cleanup aux.system/close!}

  (aux.customers/seed!)

  (flow "updates the customer"
    (http/request! :updated :put aux.customers/customer-url
                   {:body (aux.customers/->create-dto :name "João Souza")})
    (http/expect {:status 200 :body {:name "João Souza"}} :updated)))
