(ns musicaltec-app.components.card
  (:require [musicaltec-app.components.util :as u]))

(def card-class
  "bg-neutral-primary-soft border border-default rounded-base shadow-xs")

(defn card
  "Container com fundo branco, borda e sombra.

  (card attrs & body) — attrs mergeados ao default (:class é concatenado)."
  [attrs & body]
  [:div (u/merge-attrs {:class card-class} attrs)
   body])
