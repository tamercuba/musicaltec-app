(ns musicaltec-app.auth.subs
  (:require [re-frame.core :as rf]))

(rf/reg-sub :auth (fn [db _] (:auth db)))
(rf/reg-sub :auth/logged-in? (fn [db _] (get-in db [:auth :logged-in?])))
(rf/reg-sub :auth/logging-in? (fn [db _] (get-in db [:auth :logging-in?])))
(rf/reg-sub :auth/login-error (fn [db _] (get-in db [:auth :login-error])))
