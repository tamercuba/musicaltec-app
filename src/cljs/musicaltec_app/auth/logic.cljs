(ns musicaltec-app.auth.logic
  (:require [musicaltec-app.labels :as labels]))

(defn login
  [db password]
  {:db (-> db
           (assoc-in [:auth :logging-in?] true)
           (assoc-in [:auth :login-error] nil))
   :http {:method        :post
          :url           "/api/login"
          :basic-password password
          :on-success    [:auth/login-success]
          :on-failure    [:auth/login-failure]}})

(defn login-success
  [db {:keys [csrf-token]}]
  {:db (-> db
           (assoc-in [:auth :logged-in?] true)
           (assoc-in [:auth :logging-in?] false)
           (assoc-in [:auth :csrf-token] csrf-token)
           (assoc-in [:auth :login-error] nil))
   :storage/put! [:csrf-token csrf-token]
   :navigate :customers})

(defn login-failure
  [db code]
  (-> db
      (assoc-in [:auth :logging-in?] false)
      (assoc-in [:auth :login-error]
                (or (labels/label code)
                    (labels/label :auth/login-failed)))))

(defn logout
  [db]
  {:db (-> db
           (assoc-in [:auth :logged-in?] false)
           (assoc-in [:auth :csrf-token] nil)
           (assoc-in [:auth :login-error] nil))
   :storage/del! :csrf-token
   :navigate :login})

(defn expired
  [db]
  {:db (-> db
           (assoc-in [:auth :logged-in?] false)
           (assoc-in [:auth :csrf-token] nil))
   :storage/del! :csrf-token
   :navigate :login})
