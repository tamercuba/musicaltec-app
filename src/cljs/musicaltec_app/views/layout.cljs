(ns musicaltec-app.views.layout
  (:require [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]
            [musicaltec-app.components.bottom-nav :as bottom-nav]
            [musicaltec-app.components.card :as card]
            [musicaltec-app.components.icons :as icons]
            [musicaltec-app.components.theme-toggle :as theme-toggle]
            [musicaltec-app.config :as config]))

(defn navbar []
  [:nav {:class "fixed top-0 z-40 w-full bg-neutral-primary-soft border-b border-default"}
   [:div {:class "flex h-16 items-center justify-between px-4"}
    [:a {:class "flex items-center" :href (rfe/href :home)}
     icons/logo
     [:span {:class "ms-3 self-center text-xl font-semibold text-heading whitespace-nowrap"}
      config/app-name]]
    (theme-toggle/theme-toggle)]])

(defn nav-items [active-name]
  [{:label "Início" :href (rfe/href :home) :icon-path icons/home-path :active? (= active-name :home)}
   {:label "OS" :href "#" :icon-path icons/os-path :active? false}
   {:label "Vendas" :href "#" :icon-path icons/sales-path :active? false}
   {:label "Estoque" :href "#" :icon-path icons/inventory-path :active? false}
   {:label "Financeiro" :href "#" :icon-path icons/finances-path :active? false}
   {:label "Clientes" :href (rfe/href :customers) :icon-path icons/users-path :active? (= active-name :customers)}])

(defn page [& body]
  (let [active-name @(rf/subscribe [:route/name])]
    [:div
     (navbar)
     (bottom-nav/bottom-nav {:items (nav-items active-name)})
     [:div {:class "min-h-screen bg-neutral-secondary-soft pt-16 pb-16"}
      (into [:main {:class "p-4"}] body)]]))

(defn home []
  (page
   (card/card {:class "p-6"}
              [:h1 {:class "text-2xl font-semibold text-heading"} "Início"]
              [:p {:class "mt-2 text-body"} "Bem-vindo ao Musical Tec."])))
