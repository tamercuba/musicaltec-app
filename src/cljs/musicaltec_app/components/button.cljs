(ns musicaltec-app.components.button
  (:require [clojure.string :as str]
            [musicaltec-app.components.util :as u]))

(def shape-class
  "box-border border focus:ring-4 font-medium leading-5 rounded-base focus:outline-none")

(def kind-class
  {:primary   "text-white bg-brand border-transparent hover:bg-brand-strong focus:ring-brand-medium shadow-xs"
   :secondary "text-body bg-neutral-secondary-medium border-default-medium hover:bg-neutral-tertiary-medium hover:text-heading focus:ring-neutral-tertiary shadow-xs"
   :tertiary  "text-body bg-neutral-primary-soft border-default hover:bg-neutral-secondary-medium hover:text-heading focus:ring-neutral-tertiary-soft shadow-xs"
   :success   "text-white bg-success border-transparent hover:bg-success-strong focus:ring-success-medium shadow-xs"
   :danger    "text-white bg-danger border-transparent hover:bg-danger-strong focus:ring-danger-medium shadow-xs"
   :ghost     "text-heading bg-transparent border-transparent hover:bg-neutral-secondary-medium focus:ring-neutral-tertiary"})

(def size-class
  {:xs   "text-xs px-3 py-1.5"
   :sm   "text-sm px-3 py-2"
   :base "text-sm px-4 py-2.5"
   :lg   "text-base px-5 py-3"
   :xl   "text-base px-6 py-3.5"})

(defn- button-class [kind size]
  (str/join " " [shape-class (kind-class kind) (size-class size)]))

(defn button
  "Renders a Flowbite `<button>`.

  opts — {:label \"texto\"
          :type  :button | :submit | :reset   (default :button)
          :kind  :primary | :secondary | :tertiary | :success | :danger | :ghost
          :size  :xs | :sm | :base | :lg | :xl
          ...}
  As demais chaves são mergeadas como atributos HTML (ex.: :class, :disabled,
  :on-click)."
  [{:keys [label type kind size] :or {type :button kind :primary size :base} :as opts}]
  [:button (u/merge-attrs {:type (name type) :class (button-class kind size)}
                          (dissoc opts :label :type :kind :size))
   label])

(defn button-link
  "Igual a `button`, mas renderiza um `<a>` (para navegação). Requer :href."
  [{:keys [label href kind size] :or {kind :primary size :base} :as opts}]
  [:a (u/merge-attrs {:href href :class (button-class kind size)}
                     (dissoc opts :label :href :kind :size))
   label])

(def link-class "font-medium text-fg-brand hover:underline")
(def link-danger-class "font-medium text-fg-danger-strong hover:underline")

(defn link
  "Renders a text link (`<a>`).

  opts — {:label \"texto\" :href \"/x\" :kind :brand | :danger (default :brand) ...}"
  [{:keys [label href kind] :or {kind :brand} :as opts}]
  [:a (u/merge-attrs {:href href
                      :class (if (= kind :danger) link-danger-class link-class)}
                     (dissoc opts :label :href :kind))
   label])
