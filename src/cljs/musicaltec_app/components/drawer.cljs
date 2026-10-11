(ns musicaltec-app.components.drawer
  (:require [musicaltec-app.components.icons :as icons]
            [musicaltec-app.labels :as labels]))

(def base-class
  "fixed top-0 right-0 z-50 h-screen p-4 overflow-y-auto transition-transform bg-neutral-primary-soft w-96")

(def closed-class
  "translate-x-full")

(def open-class
  "translate-x-0")

(def backdrop-class
  "fixed inset-0 z-[45] bg-dark-backdrop/70 transition-opacity")

(def backdrop-open-class
  "opacity-100")

(def backdrop-closed-class
  "opacity-0 pointer-events-none")

(def header-class
  "border-b border-default pb-4 mb-5 flex items-center")

(def close-button-class
  "text-body bg-transparent hover:text-heading hover:bg-neutral-tertiary rounded-base w-9 h-9 absolute top-2.5 end-2.5 flex items-center justify-center")

(defn drawer
  "Right-side drawer (off-canvas).

  opts — {:id \"x\" :title \"...\" :body <hiccup> :open? bool :on-close fn}"
  [{:keys [id title body open? on-close]}]
  [:div
   [:div {:class (str backdrop-class " " (if open? backdrop-open-class backdrop-closed-class))
          :on-click (when open? on-close)}]
   [:div {:id id
          :class (str base-class " " (if open? open-class closed-class))
          :tabindex "-1" :aria-labelledby (str id "-label")
          :aria-hidden (if open? "false" "true")}
    [:div {:class header-class}
     [:h5 {:id (str id "-label") :class "inline-flex items-center text-lg font-medium text-body"} title]
     [:button {:type "button" :aria-controls id :class close-button-class
               :on-click on-close}
      icons/close-icon
      [:span {:class "sr-only"} (labels/label :base/close)]]]
    body]])
