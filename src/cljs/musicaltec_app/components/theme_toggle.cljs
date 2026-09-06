(ns musicaltec-app.components.theme-toggle
  (:require [re-frame.core :as rf]
            [musicaltec-app.components.icons :as icons]))

(def button-class
  "text-body hover:text-heading hover:bg-neutral-tertiary focus:outline-none focus:ring-4 focus:ring-neutral-tertiary rounded-base text-sm p-2.5")

(defn theme-toggle
  "Botão para alternar entre modo claro e escuro (dark mode)."
  []
  (let [dark? @(rf/subscribe [:theme/dark?])]
    [:button {:type "button" :class button-class
              :aria-label "Alternar modo claro/escuro"
              :on-click #(rf/dispatch [:theme/toggle])}
     (if dark? icons/sun-icon icons/moon-icon)]))
