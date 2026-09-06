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
  "`attrs` mergeados; conteúdo passado como body."
  [attrs & body]
  [:label (u/merge-attrs {:class label-class} attrs) body])

(defn input
  "Campo de texto/email/etc. `attrs` mergeados."
  [attrs]
  [:input (u/merge-attrs {:class input-class} attrs)])

(defn icon-input
  "Input com ícone à esquerda (padrão phone-input da doc).

  opts — {:icon <hiccup-svg> ...demais chaves viram atributos do <input>}"
  [{:keys [icon] :as attrs}]
  [:div {:class "relative"}
   [:div {:class "absolute inset-y-0 start-0 top-0 flex items-center ps-3.5 pointer-events-none"}
    icon]
   [:input (u/merge-attrs {:class input-icon-class} (dissoc attrs :icon))]])

(defn select
  "Select; opções passadas como body."
  [attrs & body]
  [:select (u/merge-attrs {:class select-class} attrs) body])

(defn textarea
  "Textarea; conteúdo passado como body."
  [attrs & body]
  [:textarea (u/merge-attrs {:class "block bg-neutral-secondary-medium border border-default-medium text-heading text-sm rounded-base focus:ring-brand focus:border-brand w-full p-3.5 shadow-xs placeholder:text-body"}
                            attrs) body])
