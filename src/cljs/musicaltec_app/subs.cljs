(ns musicaltec-app.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub :auth (fn [db _] (:auth db)))
(rf/reg-sub :auth/logged-in? (fn [db _] (get-in db [:auth :logged-in?])))
(rf/reg-sub :auth/logging-in? (fn [db _] (get-in db [:auth :logging-in?])))
(rf/reg-sub :auth/login-error (fn [db _] (get-in db [:auth :login-error])))

(rf/reg-sub :route (fn [db _] (:route db)))
(rf/reg-sub :route/name (fn [db _] (get-in db [:route :name])))

(rf/reg-sub :theme/dark? (fn [db _] (get-in db [:theme :dark?])))

(rf/reg-sub :customers (fn [db _] (:customers db)))
(rf/reg-sub :customers/items (fn [db _] (get-in db [:customers :items])))
(rf/reg-sub :customers/query (fn [db _] (get-in db [:customers :query])))
(rf/reg-sub :customers/page (fn [db _] (get-in db [:customers :page])))
(rf/reg-sub :customers/total-pages (fn [db _] (get-in db [:customers :total-pages])))
(rf/reg-sub :customers/total (fn [db _] (get-in db [:customers :total])))
(rf/reg-sub :customers/per-page (fn [db _] (get-in db [:customers :per-page])))
(rf/reg-sub :customers/loading? (fn [db _] (get-in db [:customers :loading?])))
(rf/reg-sub :customers/error (fn [db _] (get-in db [:customers :error])))
(rf/reg-sub :customers/drawer (fn [db _] (get-in db [:customers :drawer])))
(rf/reg-sub :customers/drawer-form (fn [db _] (get-in db [:customers :drawer :form])))
(rf/reg-sub :customers/drawer-saving? (fn [db _] (get-in db [:customers :drawer :saving?])))
