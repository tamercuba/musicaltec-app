(ns musicaltec-app.integration.customers.get-test
  (:require [state-flow.cljtest :refer [defflow]]
            [state-flow.core :refer [flow]]
            [musicaltec-app.aux.customers :as aux.customers]
            [musicaltec-app.aux.http :as http]
            [musicaltec-app.aux.system :as aux.system]))

(defflow get
  {:init   (constantly (aux.system/new-context))
   :cleanup aux.system/close!}

  (aux.customers/seed!)

  (flow "returns the customer"
    (http/request! :got :get aux.customers/customer-url)
    (http/expect {:status 200 :body {:name "João da Silva"}} :got))

  (flow "returns 404 for an unknown id"
    (http/request! :missing :get "/api/customers/00000000-0000-0000-0000-000000000000")
    (http/expect {:status 404} :missing)))
