(ns musicaltec-app.stock.events
  (:require [re-frame.core :as rf]
            [musicaltec-app.stock.logic :as logic.stock]))

(rf/reg-event-fx
 :stock/load
 (fn [{:keys [db]} _] (logic.stock/load db)))

(rf/reg-event-db
 :stock/load-success
 (fn [db [_ payload]] (logic.stock/load-success db payload)))

(rf/reg-event-db
 :stock/load-failure
 (fn [db _] (logic.stock/load-failure db)))

(rf/reg-event-fx
 :stock/set-tab
 (fn [{:keys [db]} [_ tab]] (logic.stock/set-tab db tab)))

(rf/reg-event-db
 :stock/set-query
 (fn [db [_ q]] (logic.stock/set-query db q)))

(rf/reg-event-fx
 :stock/submit-search
 (fn [{:keys [db]} _] (logic.stock/submit-search db)))

(rf/reg-event-fx
 :stock/set-page
 (fn [{:keys [db]} [_ page]] (logic.stock/set-page db page)))

(rf/reg-event-db
 :stock/open-edit
 (fn [db [_ item]] (logic.stock/open-edit db item)))

(rf/reg-event-db
 :stock/close-item-drawer
 (fn [db _] (logic.stock/close-item-drawer db)))

(rf/reg-event-db
 :stock/item-form-update
 (fn [db [_ field value]] (logic.stock/item-form-update db field value)))

(rf/reg-event-fx
 :stock/save-item
 (fn [{:keys [db]} _] (logic.stock/save-item db)))

(rf/reg-event-fx
 :stock/save-item-success
 (fn [{:keys [db]} _] (logic.stock/save-item-success db)))

(rf/reg-event-db
 :stock/save-item-failure
 (fn [db _] (logic.stock/save-item-failure db)))

(rf/reg-event-db
 :stock/open-purchase
 (fn [db [_ item]] (logic.stock/open-purchase db item)))

(rf/reg-event-db
 :stock/open-sale
 (fn [db [_ item]] (logic.stock/open-sale db item)))

(rf/reg-event-db
 :stock/open-new-item
 (fn [db _] (logic.stock/open-new-item db)))

(rf/reg-event-db
 :stock/close-movement-drawer
 (fn [db _] (logic.stock/close-movement-drawer db)))

(rf/reg-event-db
 :stock/movement-form-update
 (fn [db [_ field value]] (logic.stock/movement-form-update db field value)))

(rf/reg-event-fx
 :stock/save-movement
 (fn [{:keys [db]} _] (logic.stock/save-movement db)))

(rf/reg-event-fx
 :stock/save-movement-success
 (fn [{:keys [db]} _] (logic.stock/save-movement-success db)))

(rf/reg-event-db
 :stock/save-movement-failure
 (fn [db [_ {:keys [code]}]] (logic.stock/save-movement-failure db code)))

(rf/reg-event-fx
 :stock/search-counterparty
 (fn [_ [_ q]] (logic.stock/search-counterparty q)))

(rf/reg-event-db
 :stock/search-counterparty-success
 (fn [db [_ payload]] (logic.stock/search-counterparty-success db payload)))

(rf/reg-event-db
 :stock/search-counterparty-failure
 (fn [db _] (logic.stock/search-counterparty-failure db)))

(rf/reg-event-db
 :stock/clear-counterparty-results
 (fn [db _] (logic.stock/clear-counterparty-results db)))

(rf/reg-event-db
 :stock/select-counterparty
 (fn [db [_ name]] (logic.stock/select-counterparty db name)))
