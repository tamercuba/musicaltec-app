(ns musicaltec-app.views.customers
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [musicaltec-app.components.alert :as alert]
            [musicaltec-app.components.button :as button]
            [musicaltec-app.components.card :as card]
            [musicaltec-app.components.drawer :as drawer]
            [musicaltec-app.components.form :as form]
            [musicaltec-app.components.icons :as icons]
            [musicaltec-app.components.pagination :as pagination]
            [musicaltec-app.components.spinner :as spinner]
            [musicaltec-app.components.table :as table]
            [musicaltec-app.views.layout :as layout]))

(defn- search-form [query]
  [:form {:on-submit (fn [e]
                       (.preventDefault e)
                       (rf/dispatch [:customers/submit-search]))
          :class "flex items-center gap-2 w-full sm:w-auto"}
   [:div {:class "w-full sm:w-64"}
    (form/icon-input
     {:icon icons/search-icon :type "search" :name "q"
      :placeholder "Buscar nome ou telefone" :value query
      :on-change #(rf/dispatch [:customers/set-query (.. % -target -value)])})]
   (button/button {:label "Buscar" :type "submit" :kind :secondary})])

(defn- customer-form []
  (let [{:keys [name phones email kind tax-id]} @(rf/subscribe [:customers/drawer-form])
        saving? @(rf/subscribe [:customers/drawer-saving?])]
    [:form {:on-submit (fn [e]
                         (.preventDefault e)
                         (rf/dispatch [:customers/save]))}
     (form/label {:for "customer-name"} "Nome")
     (form/input {:type "text" :name "name" :id "customer-name"
                  :value name
                  :on-change #(rf/dispatch [:customers/form-update :name (.. % -target -value)])
                  :required true})
     [:div {:class "h-3"}]
     (form/label {} "Telefone(s)")
     [:div {:class "space-y-2"}
      (doall
       (map-indexed
        (fn [i phone]
          [:div {:key i :class "flex gap-2"}
           (form/icon-input
            {:icon icons/phone-icon :type "tel" :name "phone"
             :placeholder "Telefone" :value phone
             :on-change #(rf/dispatch [:customers/form-set-phone i (.. % -target -value)])})
           (button/button {:label "Remover" :kind :secondary :type "button"
                           :on-click #(rf/dispatch [:customers/remove-phone i])})])
        phones))]
     [:div {:class "mt-2"}
      (button/button {:label "Adicionar telefone" :kind :secondary :type "button"
                      :on-click #(rf/dispatch [:customers/add-phone])})]
     [:div {:class "h-3"}]
     (form/label {:for "customer-email"} "E-mail")
     (form/icon-input
      {:icon icons/email-icon :type "email" :name "email" :id "customer-email"
       :placeholder "email@exemplo.com" :value email
       :on-change #(rf/dispatch [:customers/form-update :email (.. % -target -value)])})
     [:div {:class "h-3"}]
     (form/label {:for "customer-kind"} "Tipo")
     (form/select {:name "kind" :id "customer-kind"
                   :value (cljs.core/name kind)
                   :on-change #(rf/dispatch [:customers/form-set-kind (keyword (.. % -target -value))])}
                  [:option {:value "person"} "Pessoa física"]
                  [:option {:value "company"} "Pessoa jurídica"])
     [:div {:class "h-3"}]
     (form/label {:for "customer-tax-id"} (if (= kind :company) "CNPJ" "CPF"))
     (form/icon-input
      {:icon icons/tax-id-icon :type "text" :name "tax-id" :id "customer-tax-id"
       :value tax-id
       :placeholder (if (= kind :company) "00.000.000/0000-00" "000.000.000-00")
       :on-change #(rf/dispatch [:customers/form-update :tax-id (.. % -target -value)])})
     [:div {:class "h-4"}]
     (button/button {:label (if saving? "Salvando..." "Salvar") :type "submit" :kind :primary
                     :disabled saving?})]))

(defn- customer-drawer []
  (let [{:keys [open? mode error]} @(rf/subscribe [:customers/drawer])]
    (drawer/drawer
     {:id "customer-drawer"
      :title (if (= mode :edit) "Editar cliente" "Novo cliente")
      :open? open?
      :on-close #(rf/dispatch [:customers/close-drawer])
      :body [:div
             (when error
               (alert/alert {:kind :danger :body error}))
             (customer-form)]})))

(defn customers-page []
  (let [items       @(rf/subscribe [:customers/items])
        query       @(rf/subscribe [:customers/query])
        page        @(rf/subscribe [:customers/page])
        total-pages @(rf/subscribe [:customers/total-pages])
        total       @(rf/subscribe [:customers/total])
        per-page    @(rf/subscribe [:customers/per-page])
        loading?    @(rf/subscribe [:customers/loading?])
        error       @(rf/subscribe [:customers/error])]
    (layout/page
     (card/card {:class "overflow-hidden"}
                [:div {:class "flex flex-col md:flex-row items-center justify-between space-y-3 md:space-y-0 md:space-x-4 p-4"}
                 [:h1 {:class "text-2xl font-semibold text-heading"} "Clientes"]
                 [:div {:class "flex flex-col sm:flex-row items-center gap-2 w-full md:w-auto"}
                  (search-form query)
                  (button/button {:label "Novo cliente" :kind :primary :type "button"
                                  :on-click #(rf/dispatch [:customers/open-create])})]]
                (when error
                  (alert/alert {:kind :danger :body error}))
                (cond
                  loading?
                  [:div {:class "p-6 flex justify-center"} (spinner/spinner)]

                  (empty? items)
                  [:p {:class "p-6 text-body"}
                   (if (str/blank? query)
                     "Nenhum cliente cadastrado."
                     "Nenhum cliente encontrado.")]

                  :else
                  [:div {:class "overflow-x-auto"}
                   (table/table
                    {}
                    (table/thead
                     {}
                     (table/tr
                      {}
                      (table/th {} "Nome")
                      (table/th {} "Telefone")
                      (table/th {} "E-mail")
                      (table/th {:class "text-right"} "Ações")))
                    (table/tbody
                     {}
                     (for [c items]
                       (table/tr
                        {:key (:id c)}
                        (table/th-row {} (:name c))
                        (table/td {} (str/join ", " (:phones c)))
                        (table/td {} (:email c))
                        (table/td
                         {:class "text-right"}
                         [:div {:class "flex items-center justify-end gap-2"}
                          (button/button
                           {:label "Editar" :kind :secondary :size :sm :type "button"
                            :on-click #(rf/dispatch [:customers/open-edit c])})
                          (button/button
                           {:label "Excluir" :kind :danger :size :sm :type "button"
                            :on-click #(when (js/window.confirm "Excluir este cliente?")
                                         (rf/dispatch [:customers/delete (:id c)]))})])))))])
                (pagination/pagination
                 {:page page :total-pages total-pages :total total :per-page per-page
                  :on-page-change #(rf/dispatch [:customers/set-page %])}))
     (customer-drawer))))
