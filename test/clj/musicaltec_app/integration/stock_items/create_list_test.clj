(ns musicaltec-app.integration.stock-items.create-list-test
  (:require [state-flow.cljtest :refer [defflow]]
            [state-flow.core :refer [flow]]
            [musicaltec-app.aux.http :as http]
            [musicaltec-app.aux.stock-items :as aux.stock-items]
            [musicaltec-app.aux.system :as aux.system]))

(defflow create-and-list
  {:init   (constantly (aux.system/new-context))
   :cleanup aux.system/close!}

  (aux.stock-items/seed!)

  (flow "lists the created stock item"
        (http/request! :listed :get "/api/stock-items")
        (http/expect {:status 200 :body {:total 1 :items [{:name "Sax Alto" :available? true}]}}
                     :listed))

  (flow "returns the stock item by id"
        (http/request! :got :get aux.stock-items/stock-item-url)
        (http/expect {:status 200 :body {:name "Sax Alto" :cost 100000}} :got)))
