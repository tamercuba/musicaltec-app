(ns musicaltec-app.core.theme
  "Dark mode. A classe `dark` é aplicada no <html> (o index.html já a define
  antes do render, evitando FOUC). A preferência é persistida em localStorage."
  (:require [re-frame.core :as rf]
            [musicaltec-app.core.storage :as storage]))

(defn current-dark? []
  (.contains (.-classList js/document.documentElement) "dark"))

(defn apply-dark! [dark?]
  (.toggle (.-classList js/document.documentElement) "dark" (boolean dark?)))

(defn toggle! []
  (let [dark? (not (current-dark?))]
    (apply-dark! dark?)
    (storage/put! :color-theme (if dark? "dark" "light"))
    dark?))

(rf/reg-event-db
 :theme/toggle
 (fn [db _]
   (assoc-in db [:theme :dark?] (toggle!))))

(rf/reg-sub :theme/dark? (fn [db _] (get-in db [:theme :dark?])))
