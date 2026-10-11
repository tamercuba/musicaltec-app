(ns musicaltec-app.core.storage
  "Wrappers around localStorage (tolerate environments where it does not exist)."
  (:require [re-frame.core :as rf]))

(defn get-item
  [k]
  (try
    (.getItem js/localStorage (name k))
    (catch js/Error _ nil)))

(defn put!
  [k v]
  (try
    (.setItem js/localStorage (name k) (str v))
    (catch js/Error _ nil)))

(defn del!
  [k]
  (try
    (.removeItem js/localStorage (name k))
    (catch js/Error _ nil)))

(rf/reg-fx
 :storage/put!
 (fn [[k v]] (put! k v)))

(rf/reg-fx
 :storage/del!
 (fn [k] (del! k)))
