(ns musicaltec-app.auth.events
  (:require [re-frame.core :as rf]
            [musicaltec-app.core.storage :as storage]))

(rf/reg-event-fx
 :auth/login
 (fn [{:keys [db]} [_ password]]
   {:db (-> db
            (assoc-in [:auth :logging-in?] true)
            (assoc-in [:auth :login-error] nil))
    :http {:method :post
           :url "/api/login"
           :basic-password password
           :on-success [:auth/login-success]
           :on-failure [:auth/login-failure]}}))

(rf/reg-event-fx
 :auth/login-success
 (fn [{:keys [db]} [_ {:keys [csrf-token]}]]
   (storage/put! :csrf-token csrf-token)
   {:db (-> db
            (assoc-in [:auth :logged-in?] true)
            (assoc-in [:auth :logging-in?] false)
            (assoc-in [:auth :csrf-token] csrf-token)
            (assoc-in [:auth :login-error] nil))
    :navigate :customers}))

(rf/reg-event-db
 :auth/login-failure
 (fn [db [_ {:keys [code]}]]
   (-> db
       (assoc-in [:auth :logging-in?] false)
       (assoc-in [:auth :login-error]
                 (case code
                   :auth/invalid-credentials "Senha incorreta."
                   "Não foi possível entrar.")))))

(rf/reg-event-fx
 :auth/logout
 (fn [{:keys [db]} _]
   (storage/del! :csrf-token)
   {:db (-> db
            (assoc-in [:auth :logged-in?] false)
            (assoc-in [:auth :csrf-token] nil)
            (assoc-in [:auth :login-error] nil))
    :navigate :login}))

(rf/reg-event-fx
 :auth/expired
 (fn [{:keys [db]} _]
   (storage/del! :csrf-token)
   {:db (-> db
            (assoc-in [:auth :logged-in?] false)
            (assoc-in [:auth :csrf-token] nil))
    :navigate :login}))
