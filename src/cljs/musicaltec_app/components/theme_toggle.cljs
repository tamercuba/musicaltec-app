(ns musicaltec-app.components.theme-toggle
  (:require [re-frame.core :as rf]
            [musicaltec-app.components.icons :as icons]
            [musicaltec-app.labels :as labels]))

(def button-class
  "text-body hover:text-heading hover:bg-neutral-tertiary focus:outline-none focus:ring-4 focus:ring-neutral-tertiary rounded-base text-sm p-2.5")

(defn theme-toggle
  "Button to toggle between light and dark mode."
  []
  (let [dark? @(rf/subscribe [:theme/dark?])]
    [:button {:type "button" :class button-class
              :aria-label (labels/label :base/toggle-theme)
              :on-click #(rf/dispatch [:theme/toggle])}
     (if dark? icons/sun-icon icons/moon-icon)]))
