(ns musicaltec-app.components.card
  (:require [musicaltec-app.components.util :as u]))

(def card-class
  "bg-neutral-primary-soft border border-default rounded-base shadow-xs")

(defn card
  "Container with white background, border and shadow.

  (card attrs & body) — attrs merged into the default (:class is concatenated)."
  [attrs & body]
  [:div (u/merge-attrs {:class card-class} attrs)
   body])
