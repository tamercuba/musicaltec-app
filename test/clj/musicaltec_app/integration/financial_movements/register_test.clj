(ns musicaltec-app.integration.financial-movements.register-test
  (:require [state-flow.cljtest :refer [defflow]]
            [state-flow.core :refer [flow]]
            [state-flow.state :as state]
            [musicaltec-app.aux.http :as http]
            [musicaltec-app.aux.stock-items :as aux.stock-items]
            [musicaltec-app.aux.system :as aux.system]))

(defflow register-sale-decrements-stock
  {:init   (constantly (aux.system/new-context))
   :cleanup aux.system/close!}

  (aux.stock-items/seed!)

  (flow "registers a sale (direction in) and decrements stock"
        (state/modify
         (fn [ctx]
           (let [sid (get-in ctx [:created-item :body :id])
                 resp (http/request ctx :post "/api/financial-movements"
                                    {:body {:type      "stock"
                                            :direction "in"
                                            :date      "2026-01-16T00:00:00.000-00:00"
                                            :items     [{:stock-item-id sid :quantity 1 :unit-price 150000}]}})]
             (assoc ctx :sale resp))))
        (http/expect {:status 201 :body {:type "stock" :direction "in" :amount 150000}} :sale))

  (flow "sold item is now marked sold"
        (http/request! :sold-item :get aux.stock-items/stock-item-url)
        (http/expect {:status 200 :body {:available? false :quantity 0}} :sold-item))

  (flow "lists financial movements"
        (http/request! :movements :get "/api/financial-movements")
        (http/expect {:status 200 :body {:total 1}} :movements)))

(defflow register-purchase-increments-stock
  {:init   (constantly (aux.system/new-context))
   :cleanup aux.system/close!}

  (aux.stock-items/seed!)

  (flow "registers a purchase (direction out) and increments stock"
        (state/modify
         (fn [ctx]
           (let [sid (get-in ctx [:created-item :body :id])
                 resp (http/request ctx :post "/api/financial-movements"
                                    {:body {:type      "stock"
                                            :direction "out"
                                            :date      "2026-01-15T00:00:00.000-00:00"
                                            :items     [{:stock-item-id sid :quantity 1 :unit-price 100000}]}})]
             (assoc ctx :purchase resp))))
        (http/expect {:status 201 :body {:direction "out" :amount 100000}} :purchase))

  (flow "stock quantity increased"
        (http/request! :item :get aux.stock-items/stock-item-url)
        (http/expect {:status 200 :body {:quantity 2}} :item)))

(defflow rejects-insufficient-stock
  {:init   (constantly (aux.system/new-context))
   :cleanup aux.system/close!}

  (aux.stock-items/seed!)

  (flow "rejects a sale exceeding stock"
        (state/modify
         (fn [ctx]
           (let [sid (get-in ctx [:created-item :body :id])
                 resp (http/request ctx :post "/api/financial-movements"
                                    {:body {:type      "stock"
                                            :direction "in"
                                            :date      "2026-01-16T00:00:00.000-00:00"
                                            :items     [{:stock-item-id sid :quantity 99 :unit-price 100}]}})]
             (assoc ctx :sale resp))))
        (http/expect {:status 422 :body {:code "financial-movement/stock-unavailable"}} :sale)))

(defflow register-purchase-creates-new-item
  {:init   (constantly (aux.system/new-context))
   :cleanup aux.system/close!}

  (aux.stock-items/seed!)

  (flow "creates a new item via purchase"
        (state/modify
         (fn [ctx]
           (let [resp (http/request ctx :post "/api/financial-movements"
                                    {:body {:type      "stock"
                                            :direction "out"
                                            :date      "2026-01-15T00:00:00.000-00:00"
                                            :counterparty "Celso do Gelo"
                                            :items     [{:name "Flauta" :brand "Yamaha" :quantity 1 :unit-price 80000 :default-price 120000}]}})]
             (assoc ctx :purchase resp))))
        (http/expect {:status 201 :body {:direction "out" :amount 80000 :counterparty "Celso do Gelo"}} :purchase))

  (flow "new item appears in stock"
        (http/request! :stock :get "/api/stock-items")
        (http/expect {:status 200 :body {:total 2}} :stock)))
