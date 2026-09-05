(ns musicaltec-app.integration.customers.list-test
  (:require [state-flow.cljtest :refer [defflow]]
            [state-flow.core :refer [flow]]
            [musicaltec-app.aux.customers :as aux.customers]
            [musicaltec-app.aux.http :as http]
            [musicaltec-app.aux.system :as aux.system]))

(defflow list
  {:init   (constantly (aux.system/new-context))
   :cleanup aux.system/close!}

  (aux.customers/seed!)

  (flow "lists the created customer"
    (http/request! :listed :get "/api/customers")
    (http/expect {:status 200 :body {:total 1 :items [{:name "João da Silva"}]}}
                 :listed)))
