(ns musicaltec-app.components.form
  (:require [musicaltec-app.components.util :as u]))

(def label-class
  "block mb-2.5 text-sm font-medium text-heading")

(def input-class
  "bg-neutral-secondary-medium border border-default-medium text-heading text-sm rounded-base focus:ring-brand focus:border-brand block w-full px-3 py-2.5 shadow-xs placeholder:text-body")

(def input-icon-class
  "bg-neutral-secondary-medium border border-default-medium text-heading text-sm rounded-base focus:ring-brand focus:border-brand block w-full ps-9 pe-3 py-2.5 shadow-xs placeholder:text-body")

(def select-class
  "block w-full px-3 py-2.5 bg-neutral-secondary-medium border border-default-medium text-heading text-sm rounded-base focus:ring-brand focus:border-brand shadow-xs placeholder:text-body")

(defn label
  "`attrs` merged; content passed as body."
  [attrs & body]
  [:label (u/merge-attrs {:class label-class} attrs) body])

(defn input
  "Text/email/etc. field. `attrs` merged."
  [attrs]
  [:input (u/merge-attrs {:class input-class} attrs)])

(defn icon-input
  "Input with a left icon (the doc's phone-input pattern).

  opts — {:icon <hiccup-svg> ...other keys become <input> attributes}"
  [{:keys [icon] :as attrs}]
  [:div {:class "relative"}
   [:div {:class "absolute inset-y-0 start-0 top-0 flex items-center ps-3.5 pointer-events-none"}
    icon]
   [:input (u/merge-attrs {:class input-icon-class} (dissoc attrs :icon))]])

(defn select
  "Select; options passed as body."
  [attrs & body]
  [:select (u/merge-attrs {:class select-class} attrs) body])

(defn textarea
  "Textarea; content passed as body."
  [attrs & body]
  [:textarea (u/merge-attrs {:class "block bg-neutral-secondary-medium border border-default-medium text-heading text-sm rounded-base focus:ring-brand focus:border-brand w-full p-3.5 shadow-xs placeholder:text-body"}
                            attrs) body])
