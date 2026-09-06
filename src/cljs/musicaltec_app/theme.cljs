(ns musicaltec-app.theme
  (:require [musicaltec-app.storage :as storage]))

(defn current-dark? []
  (.contains (.-classList js/document.documentElement) "dark"))

(defn apply-dark! [dark?]
  (.toggle (.-classList js/document.documentElement) "dark" (boolean dark?)))

(defn toggle! []
  (let [dark? (not (current-dark?))]
    (apply-dark! dark?)
    (storage/put! :color-theme (if dark? "dark" "light"))
    dark?))
