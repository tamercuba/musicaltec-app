(ns musicaltec-app.core
  (:require
   [re-frame.core :as rf]
   [reagent.dom :as rdom]
   [reitit.frontend.easy :as rfe]
   [musicaltec-app.auth.db :as auth-db]
   [musicaltec-app.auth.events]
   [musicaltec-app.auth.subs]
   [musicaltec-app.customers.db :as customers-db]
   [musicaltec-app.customers.events]
   [musicaltec-app.customers.subs]
   [musicaltec-app.core.db :as app-db]
   [musicaltec-app.core.http]
   [musicaltec-app.core.router :as router]
   [musicaltec-app.core.storage :as storage]
   [musicaltec-app.core.theme :as theme]
   [musicaltec-app.core.views :as views]
   [musicaltec-app.core.views.error-boundary :as error-boundary]))

(def default-db
  (merge app-db/default-db
         auth-db/default-db
         customers-db/default-db))

(rf/reg-event-fx
 :app/init
 (fn [_ _]
   (let [token (storage/get-item :csrf-token)]
     {:db (-> default-db
              (assoc-in [:auth  :csrf-token] token)
              (assoc-in [:auth  :logged-in?] (some? token))
              (assoc-in [:theme :dark?]      (theme/current-dark?)))})))

(defn ^:export init []
  (rf/dispatch-sync [:app/init])
  (rfe/start! router/router
              (fn [match _history]
                (rf/dispatch [:router/navigate match]))
              {:use-fragment false})
  (rdom/render [error-boundary/error-boundary [views/root]] (js/document.getElementById "app")))
