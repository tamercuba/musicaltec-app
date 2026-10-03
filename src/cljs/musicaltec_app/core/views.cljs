(ns musicaltec-app.core.views
  (:require [re-frame.core :as rf]
            [musicaltec-app.auth.views :as auth]
            [musicaltec-app.customers.views :as customers]
            [musicaltec-app.stock.views :as stock]
            [musicaltec-app.core.views.layout :as layout]))

(defn- not-found []
  (layout/page
   [:h1 {:class "text-lg font-semibold text-heading"} "Página não encontrada."]))

(defn root []
  (let [route-name @(rf/subscribe [:route/name])
        logged-in? @(rf/subscribe [:auth/logged-in?])]
    (cond
      (nil? route-name)         nil
      (= route-name :login)     [auth/login-page]
      (not logged-in?)          [auth/login-page]
      (= route-name :home)      [layout/home]
      (= route-name :customers) [customers/customers-page]
      (= route-name :stock)     [stock/stock-page]
      :else                     [not-found])))
