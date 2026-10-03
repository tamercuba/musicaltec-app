(ns musicaltec-app.core.router
  (:require [re-frame.core :as rf]
            [reitit.frontend :as rfr]
            [reitit.frontend.easy :as rfe]))

(def routes
  [["/" {:name :home}]
   ["/login" {:name :login}]
   ["/customers" {:name :customers}]
   ["/stock" {:name :stock}]])

(def router
  (rfr/router routes))

(rf/reg-fx
 :navigate
 (fn [route-name]
   (rfe/push-state route-name)))

(rf/reg-event-fx
 :router/navigate
 (fn [{:keys [db]} [_ match]]
   (let [route {:name (get-in match [:data :name])
                :path-params (get match :path-params {})
                :query-params (get match :query-params {})}
         load-event (case (:name route)
                      :customers :customers/load
                      :stock     :stock/load
                      nil)
         fx {:db (assoc db :route route)}]
     (if (and load-event (get-in db [:auth :logged-in?]))
       (assoc fx :dispatch [load-event])
       fx))))

(rf/reg-sub :route      (fn [db _] (:route db)))
(rf/reg-sub :route/name (fn [db _] (get-in db [:route :name])))
