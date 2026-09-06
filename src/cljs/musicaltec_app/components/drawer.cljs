(ns musicaltec-app.components.drawer
  (:require [musicaltec-app.components.icons :as icons]))

(def base-class
  "fixed top-0 right-0 z-50 h-screen p-4 overflow-y-auto transition-transform bg-neutral-primary-soft w-96")

(def closed-class
  "translate-x-full")

(def open-class
  "translate-x-0")

(def backdrop-class
  "fixed inset-0 z-[45] bg-neutral-quaternary bg-opacity-50")

(def header-class
  "border-b border-default pb-4 mb-5 flex items-center")

(def close-button-class
  "text-body bg-transparent hover:text-heading hover:bg-neutral-tertiary rounded-base w-9 h-9 absolute top-2.5 end-2.5 flex items-center justify-center")

(defn drawer
  "Drawer lateral direito (off-canvas).

  opts — {:id \"x\" :title \"...\" :body <hiccup> :open? bool :on-close fn}"
  [{:keys [id title body open? on-close]}]
  [:div
   (when open?
     [:div {:class backdrop-class :on-click on-close}])
   [:div {:id id
          :class (str base-class " " (if open? open-class closed-class))
          :tabindex "-1" :aria-labelledby (str id "-label")}
    [:div {:class header-class}
     [:h5 {:id (str id "-label") :class "inline-flex items-center text-lg font-medium text-body"} title]
     [:button {:type "button" :aria-controls id :class close-button-class
               :on-click on-close}
      icons/close-icon
      [:span {:class "sr-only"} "Fechar"]]]
    body]])
