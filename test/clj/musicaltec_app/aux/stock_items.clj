(ns musicaltec-app.aux.stock-items
  (:require [state-flow.core :refer [flow]]
            [musicaltec-app.aux.http :as http]
            [musicaltec-app.aux.system :as aux.system]))

(def create-dto
  {:name "Sax Alto" :brand "Yamaha" :cost 100000 :default-price 150000})

(defn ->create-dto [& {:as overrides}]
  (merge create-dto overrides))

(defn seed!
  "Authenticates and creates a stock item, storing the response in `:created-item`."
  []
  (flow "authenticate and create a stock item"
        (http/login! aux.system/password)
        (http/expect {:status 200} :login)
        (http/request! :created-item :post "/api/stock-items" {:body (->create-dto)})
        (http/expect {:status 201 :body {:name "Sax Alto"}} :created-item)))

(defn stock-item-url [ctx]
  (str "/api/stock-items/" (get-in ctx [:created-item :body :id])))
