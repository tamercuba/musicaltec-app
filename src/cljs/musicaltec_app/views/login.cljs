(ns musicaltec-app.views.login
  (:require [re-frame.core :as rf]
            [reagent.core :as r]
            [musicaltec-app.components.alert :as alert]
            [musicaltec-app.components.button :as button]
            [musicaltec-app.components.card :as card]
            [musicaltec-app.components.form :as form]
            [musicaltec-app.components.icons :as icons]
            [musicaltec-app.config :as config]))

(defn login-page []
  (let [password (r/atom "")]
    (fn []
      (let [error @(rf/subscribe [:auth/login-error])
            logging-in? @(rf/subscribe [:auth/logging-in?])]
        [:div {:class "min-h-screen bg-neutral-secondary-soft flex items-center justify-center p-4"}
         [:div {:class "w-full max-w-sm"}
          (card/card {:class "p-6"}
                     [:div {:class "flex items-center justify-center mb-6"}
                      icons/logo
                      [:h1 {:class "ms-3 text-xl font-semibold text-heading"} config/app-name]]
                     (when error
                       (alert/alert {:kind :danger :body error}))
                     [:form {:on-submit (fn [e]
                                          (.preventDefault e)
                                          (rf/dispatch [:auth/login @password]))}
                      (form/label {:for "password"} "Senha")
                      (form/input {:type "password" :id "password" :name "password"
                                   :placeholder "••••••••"
                                   :autoComplete "current-password"
                                   :value @password
                                   :on-change #(reset! password (.. % -target -value))
                                   :autoFocus true})
                      [:div {:class "h-4"}]
                      (button/button {:label (if logging-in? "Entrando..." "Entrar")
                                      :type "submit"
                                      :kind :primary
                                      :class "w-full"
                                      :disabled logging-in?})])]]))))
