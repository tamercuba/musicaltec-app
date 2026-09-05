(ns musicaltec-app.integration.customers.delete-test
  (:require [state-flow.cljtest :refer [defflow]]
            [state-flow.core :refer [flow]]
            [musicaltec-app.aux.customers :as aux.customers]
            [musicaltec-app.aux.http :as http]
            [musicaltec-app.aux.system :as aux.system]))

(defflow delete
  {:init   (constantly (aux.system/new-context))
   :cleanup aux.system/close!}

  (aux.customers/seed!)

  (flow "removes the customer"
    (http/request! :deleted :delete aux.customers/customer-url)
    (http/expect {:status 204} :deleted))

  (flow "returns 404 after deletion"
    (http/request! :missing :get aux.customers/customer-url)
    (http/expect {:status 404} :missing)))
