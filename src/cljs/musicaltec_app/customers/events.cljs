(ns musicaltec-app.customers.events
  (:require [re-frame.core :as rf]
            [musicaltec-app.customers.logic :as logic.customers]))

(rf/reg-event-fx
 :customers/load
 (fn [{:keys [db]} _] (logic.customers/load db)))

(rf/reg-event-db
 :customers/load-success
 (fn [db [_ payload]] (logic.customers/load-success db payload)))

(rf/reg-event-db
 :customers/load-failure
 (fn [db _] (logic.customers/load-failure db)))

(rf/reg-event-db
 :customers/set-query
 (fn [db [_ q]] (logic.customers/set-query db q)))

(rf/reg-event-fx
 :customers/submit-search
 (fn [{:keys [db]} _] (logic.customers/submit-search db)))

(rf/reg-event-fx
 :customers/set-page
 (fn [{:keys [db]} [_ page]] (logic.customers/set-page db page)))

(rf/reg-event-db
 :customers/open-create
 (fn [db _] (logic.customers/open-create db)))

(rf/reg-event-db
 :customers/open-edit
 (fn [db [_ customer]] (logic.customers/open-edit db customer)))

(rf/reg-event-db
 :customers/close-drawer
 (fn [db _] (logic.customers/close-drawer db)))

(rf/reg-event-db
 :customers/form-update
 (fn [db [_ field value]] (logic.customers/form-update db field value)))

(rf/reg-event-db
 :customers/form-set-kind
 (fn [db [_ kind]] (logic.customers/form-set-kind db kind)))

(rf/reg-event-db
 :customers/form-set-phone
 (fn [db [_ index value]] (logic.customers/form-set-phone db index value)))

(rf/reg-event-db
 :customers/add-phone
 (fn [db _] (logic.customers/add-phone db)))

(rf/reg-event-db
 :customers/remove-phone
 (fn [db [_ index]] (logic.customers/remove-phone db index)))

(rf/reg-event-fx
 :customers/save
 (fn [{:keys [db]} _] (logic.customers/save db)))

(rf/reg-event-fx
 :customers/save-success
 (fn [{:keys [db]} _] (logic.customers/save-success db)))

(rf/reg-event-db
 :customers/save-failure
 (fn [db [_ {:keys [code]}]] (logic.customers/save-failure db code)))

(rf/reg-event-fx
 :customers/delete
 (fn [_ [_ id]] (logic.customers/delete id)))

(rf/reg-event-fx
 :customers/delete-success
 (fn [_ _] (logic.customers/delete-success)))

(rf/reg-event-db
 :customers/delete-failure
 (fn [db _] (logic.customers/delete-failure db)))
