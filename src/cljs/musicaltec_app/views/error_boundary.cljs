(ns musicaltec-app.views.error-boundary
  (:require [reagent.core :as r]))

(defn error-boundary []
  (let [error (r/atom nil)]
    (r/create-class
     {:display-name "error-boundary"
      :component-did-catch
      (fn [_this e info]
        (js/console.error "Erro de renderização:" e info)
        (reset! error e))
      :reagent-render
      (fn [& children]
        (if-let [e @error]
          [:div {:style {:padding "2rem"
                         :color "#b91c1c"
                         :background "#fff"
                         :min-height "100vh"
                         :font-family "monospace"
                         :font-size "14px"}}
           [:h1 {:style {:font-size "1.5rem" :margin-bottom "1rem"}}
            "Erro de renderização"]
           [:pre {:style {:white-space "pre-wrap" :margin-bottom "1rem"}}
            (str e)]
           [:pre {:style {:white-space "pre-wrap"}}
            (some-> e .-stack)]]
          (into [:<>] children)))})))
