(ns musicaltec-app.views.core
  (:require [re-frame.core :as rf]
            [musicaltec-app.views.customers :as customers]
            [musicaltec-app.views.layout :as layout]
            [musicaltec-app.views.login :as login]))

(defn- not-found []
  (layout/page
   [:h1 {:class "text-lg font-semibold text-heading"} "Página não encontrada."]))

(defn root []
  (let [route-name @(rf/subscribe [:route/name])
        logged-in? @(rf/subscribe [:auth/logged-in?])]
    (cond
      (nil? route-name)        nil
      (= route-name :login)    [login/login-page]
      (not logged-in?)         [login/login-page]
      (= route-name :home)     [layout/home]
      (= route-name :customers) [customers/customers-page]
      :else                    [not-found])))
