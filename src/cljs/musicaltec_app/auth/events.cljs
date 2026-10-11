(ns musicaltec-app.auth.events
  (:require [re-frame.core :as rf]
            [musicaltec-app.auth.logic :as logic.auth]))

(rf/reg-event-fx
 :auth/login
 (fn [{:keys [db]} [_ password]] (logic.auth/login db password)))

(rf/reg-event-fx
 :auth/login-success
 (fn [{:keys [db]} [_ payload]] (logic.auth/login-success db payload)))

(rf/reg-event-db
 :auth/login-failure
 (fn [db [_ {:keys [code]}]] (logic.auth/login-failure db code)))

(rf/reg-event-fx
 :auth/logout
 (fn [{:keys [db]} _] (logic.auth/logout db)))

(rf/reg-event-fx
 :auth/expired
 (fn [{:keys [db]} _] (logic.auth/expired db)))
