(ns musicaltec-app.components.util
  (:require [clojure.string :as str]))

(defn merge-attrs
  "Merges `attrs` into `defaults`. `:class` values are concatenated; for any
  other key, `attrs` wins."
  [defaults attrs]
  (let [class (str/join " " (remove str/blank? [(:class defaults) (:class attrs)]))]
    (cond-> (merge defaults attrs)
      (seq class) (assoc :class class))))
