(ns musicaltec-app.stock.views
  (:require [re-frame.core :as rf]
            [musicaltec-app.components.alert :as alert]
            [musicaltec-app.components.badge :as badge]
            [musicaltec-app.components.button :as button]
            [musicaltec-app.components.card :as card]
            [musicaltec-app.components.datepicker :as datepicker]
            [musicaltec-app.components.drawer :as drawer]
            [musicaltec-app.components.form :as form]
            [musicaltec-app.components.pagination :as pagination]
            [musicaltec-app.components.spinner :as spinner]
            [musicaltec-app.components.table :as table]
            [musicaltec-app.core.format :as format]
            [musicaltec-app.core.views.layout :as layout]))

(defn- money [cents]
  (format/cents->brl cents))

(defn- search-form [query]
  [:form {:on-submit (fn [e] (.preventDefault e) (rf/dispatch [:stock/submit-search]))
          :class "flex items-center gap-2 w-full sm:w-auto"}
   [:div {:class "w-full sm:w-64"}
    (form/input {:type "search" :name "q" :placeholder "Buscar por nome" :value query
                 :on-change #(rf/dispatch [:stock/set-query (.. % -target -value)])})]
   (button/button {:label "Buscar" :type "submit" :kind :secondary})])

(defn- tabs [tab]
  [:div {:class "flex gap-2 border-b border-default"}
   (button/button {:label "Em estoque" :kind (if (= tab :available) :primary :ghost)
                   :type "button" :on-click #(rf/dispatch [:stock/set-tab :available])})
   (button/button {:label "Esgotados" :kind (if (= tab :sold-out) :primary :ghost)
                   :type "button" :on-click #(rf/dispatch [:stock/set-tab :sold-out])})])

(defn- item-form []
  (let [{:keys [name brand quantity available? cost default-price serial notes]} @(rf/subscribe [:stock/item-drawer-form])
        saving? @(rf/subscribe [:stock/item-drawer-saving?])]
    [:form {:on-submit (fn [e] (.preventDefault e) (rf/dispatch [:stock/save-item]))}
     (form/label {} "Nome")
     (form/input {:type "text" :value name :required true
                  :on-change #(rf/dispatch [:stock/item-form-update :name (.. % -target -value)])})
     [:div {:class "h-3"}]
     (form/label {} "Marca")
     (form/input {:type "text" :value brand
                  :on-change #(rf/dispatch [:stock/item-form-update :brand (.. % -target -value)])})
     [:div {:class "h-3"}]
     [:div {:class "grid grid-cols-2 gap-3"}
      [:div
       (form/label {} "Quantidade")
       (form/input {:type "text" :value quantity :disabled true})]
      [:div
       (form/label {} "Disponível")
       (form/input {:type "text" :value (if available? "Sim" "Não") :disabled true})]]
     [:div {:class "h-3"}]
     (form/label {} "Custo (R$)")
     (form/input {:type "text" :value cost :placeholder "R$ 0,00"
                  :on-change #(rf/dispatch [:stock/item-form-update :cost (format/mask-brl (.. % -target -value))])})
     [:div {:class "h-3"}]
     (form/label {} "Preço de venda (R$)")
     (form/input {:type "text" :value default-price :placeholder "R$ 0,00"
                  :on-change #(rf/dispatch [:stock/item-form-update :default-price (format/mask-brl (.. % -target -value))])})
     [:div {:class "h-3"}]
     (form/label {} "Nº de série")
     (form/input {:type "text" :value serial
                  :on-change #(rf/dispatch [:stock/item-form-update :serial (.. % -target -value)])})
     [:div {:class "h-3"}]
     (form/label {} "Observações")
     (form/textarea {:value notes
                     :on-change #(rf/dispatch [:stock/item-form-update :notes (.. % -target -value)])})
     [:div {:class "h-4"}]
     (button/button {:label (if saving? "Salvando..." "Salvar") :type "submit" :kind :primary :disabled saving?})]))

(defn- item-drawer []
  (let [{:keys [open? mode error]} @(rf/subscribe [:stock/item-drawer])]
    (drawer/drawer
     {:id       "stock-item-drawer"
      :title    (if (= mode :edit) "Editar item" "Item")
      :open?    open?
      :on-close #(rf/dispatch [:stock/close-item-drawer])
      :body     [:div
                 (when (seq error)
                   (alert/alert {:kind :danger :body (into [:div {:class "space-y-1"}] (map (fn [m] [:div m]) error))}))
                 (item-form)]})))

(defn- counterparty-input [mode counterparty]
  (let [results @(rf/subscribe [:stock/counterparty-results])]
    [:div {:class "relative"}
     (form/input {:type "text" :value counterparty
                  :placeholder (if (= mode :sale) "Buscar cliente" "Buscar fornecedor")
                  :on-change (fn [e]
                               (let [v (.. e -target -value)]
                                 (rf/dispatch [:stock/movement-form-update :counterparty v])
                                 (rf/dispatch [:stock/search-counterparty v])))
                  :on-blur #(rf/dispatch [:stock/clear-counterparty-results])})
     (when (seq results)
       [:ul {:class "absolute z-10 w-full mt-1 bg-neutral-primary-soft border border-default rounded-base shadow-lg max-h-48 overflow-auto"}
        (for [c results]
          [:li {:key (:id c)
                :class "px-3 py-2 text-sm text-body cursor-pointer hover:bg-neutral-secondary-medium"
                :on-mouse-down #(rf/dispatch [:stock/select-counterparty (:name c)])}
           (:name c)])])]))

(defn- movement-form []
  (let [{:keys [mode saving? form]} @(rf/subscribe [:stock/movement-drawer])
        {:keys [stock-item-id name brand serial notes quantity unit-price default-price date counterparty parcelado? installments-count has-down-payment? down-payment available-quantity]} form]
    [:form {:on-submit (fn [e] (.preventDefault e) (rf/dispatch [:stock/save-movement]))}
     (if stock-item-id
       [:div
        (form/label {} "Item")
        [:p {:class "text-body"} name]]
       [:div
        (form/label {} "Nome")
        (form/input {:type "text" :value name :required true
                     :on-change #(rf/dispatch [:stock/movement-form-update :name (.. % -target -value)])})
        [:div {:class "h-3"}]
        (form/label {} "Marca")
        (form/input {:type "text" :value brand
                     :on-change #(rf/dispatch [:stock/movement-form-update :brand (.. % -target -value)])})
        [:div {:class "h-3"}]
        (form/label {} "Preço de venda (R$)")
        (form/input {:type "text" :value default-price
                     :on-change #(rf/dispatch [:stock/movement-form-update :default-price (format/mask-brl (.. % -target -value))])})
        [:div {:class "h-3"}]
        (form/label {} "Nº de série")
        (form/input {:type "text" :value serial
                     :on-change #(rf/dispatch [:stock/movement-form-update :serial (.. % -target -value)])})
        [:div {:class "h-3"}]
        (form/label {} "Observações")
        (form/textarea {:value notes
                        :on-change #(rf/dispatch [:stock/movement-form-update :notes (.. % -target -value)])})])
     [:div {:class "h-3"}]
     (form/label {} "Quantidade")
     (form/input (cond-> {:type "number" :min "1" :value quantity
                          :on-change #(rf/dispatch [:stock/movement-form-update :quantity (.. % -target -value)])}
                   (= mode :sale) (assoc :max available-quantity)))
     (when (= mode :sale)
       [:p {:class "text-xs text-body"} (str "Disponível: " available-quantity)])
     [:div {:class "h-3"}]
     (form/label {} (if (= mode :sale) "Preço de venda (R$)" "Preço de compra (R$)"))
     (form/input {:type "text" :value unit-price :placeholder "R$ 0,00"
                  :on-change #(rf/dispatch [:stock/movement-form-update :unit-price (format/mask-brl (.. % -target -value))])})
     [:div {:class "h-3"}]
     (form/label {} "Data")
     (datepicker/datepicker {:value date
                             :on-change #(rf/dispatch [:stock/movement-form-update :date %])})
     [:div {:class "h-3"}]
     (form/label {} (if (= mode :sale) "Cliente" "Fornecedor"))
     (counterparty-input mode counterparty)
     [:div {:class "h-3"}]
     [:label {:class "flex items-center gap-2 text-heading"}
      [:input {:type "checkbox" :checked parcelado?
               :class "w-4 h-4 border border-default-medium rounded-xs bg-neutral-secondary-medium focus:ring-2 focus:ring-brand-soft"
               :on-change #(rf/dispatch [:stock/movement-form-update :parcelado? (.. % -target -checked)])}]
      "Parcelado?"]
     (when parcelado?
       [:div
        [:div {:class "h-3"}]
        (form/label {} "Nº de parcelas")
        (form/select {:value installments-count
                      :on-change #(rf/dispatch [:stock/movement-form-update :installments-count (.. % -target -value)])}
                     (for [n (range 2 13)] [:option {:key n :value (str n)} n]))
        [:div {:class "h-3"}]
        [:label {:class "flex items-center gap-2 text-heading"}
         [:input {:type "checkbox" :checked has-down-payment?
                  :class "w-4 h-4 border border-default-medium rounded-xs bg-neutral-secondary-medium focus:ring-2 focus:ring-brand-soft"
                  :on-change #(rf/dispatch [:stock/movement-form-update :has-down-payment? (.. % -target -checked)])}]
         "Valor de Entrada?"]
        (when has-down-payment?
          [:div
           [:div {:class "h-3"}]
           (form/label {} "Entrada (R$)")
           (form/input {:type "text" :value down-payment :placeholder "R$ 0,00"
                        :on-change #(rf/dispatch [:stock/movement-form-update :down-payment (format/mask-brl (.. % -target -value))])})])])
     [:div {:class "h-4"}]
     (button/button {:label (if saving? "Salvando..." "Salvar") :type "submit" :kind :primary :disabled saving?})]))

(defn- movement-drawer []
  (let [{:keys [open? mode error]} @(rf/subscribe [:stock/movement-drawer])]
    (drawer/drawer
     {:id       "stock-movement-drawer"
      :title    (case mode :purchase "Nova compra" :sale "Nova venda" :new-item "Novo item" "")
      :open?    open?
      :on-close #(rf/dispatch [:stock/close-movement-drawer])
      :body     [:div
                 (when (seq error)
                   (alert/alert {:kind :danger :body (into [:div {:class "space-y-1"}] (map (fn [m] [:div m]) error))}))
                 (movement-form)]})))

(defn stock-page []
  (let [items       @(rf/subscribe [:stock/items])
        tab         @(rf/subscribe [:stock/tab])
        query       @(rf/subscribe [:stock/query])
        page        @(rf/subscribe [:stock/page])
        total-pages @(rf/subscribe [:stock/total-pages])
        total       @(rf/subscribe [:stock/total])
        per-page    @(rf/subscribe [:stock/per-page])
        loading?    @(rf/subscribe [:stock/loading?])
        error       @(rf/subscribe [:stock/error])]
    (layout/page
     (card/card {:class "overflow-hidden"}
                [:div {:class "flex flex-col md:flex-row items-center justify-between space-y-3 md:space-y-0 md:space-x-4 p-4"}
                 [:h1 {:class "text-2xl font-semibold text-heading"} "Estoque"]
                 [:div {:class "flex flex-col sm:flex-row items-center gap-2 w-full md:w-auto"}
                  (search-form query)
                  (button/button {:label "Novo item" :kind :primary :type "button"
                                  :on-click #(rf/dispatch [:stock/open-new-item])})]]
                [:div {:class "px-4 pb-2"}
                 (tabs tab)]
                (when error
                  (alert/alert {:kind :danger :body error}))
                (cond
                  loading?
                  [:div {:class "p-6 flex justify-center"} (spinner/spinner)]

                  (empty? items)
                  [:p {:class "p-6 text-body"}
                   (if (= tab :available) "Nenhum item em estoque." "Nenhum item esgotado.")]

                  :else
                  [:div {:class "overflow-x-auto"}
                   (table/table
                    {}
                    (table/thead
                     {}
                     (table/tr
                      {}
                      (table/th {} "Nome")
                      (table/th {} "Marca")
                      (table/th {} "Qtd")
                      (table/th {} "Custo")
                      (table/th {} "Preço")
                      (table/th {} "Status")
                      (table/th {:class "text-right"} "Ações")))
                    (table/tbody
                     {}
                     (for [i items]
                       (table/tr
                        {:key (:id i)}
                        (table/th-row {} (:name i))
                        (table/td {} (:brand i))
                        (table/td {} (:quantity i))
                        (table/td {} (money (:cost i)))
                        (table/td {} (money (:default-price i)))
                        (table/td {} (if (:available? i)
                                       (badge/badge {:kind :success :label "Em estoque"})
                                       (badge/badge {:kind :warning :label "Esgotado"})))
                        (table/td {:class "text-right"}
                                  [:div {:class "flex items-center justify-end gap-2"}
                                   (button/button {:label (if (:available? i) "Comprar" "Reabastecer")
                                                   :kind :primary :size :sm :type "button"
                                                   :on-click #(rf/dispatch [:stock/open-purchase i])})
                                   (when (:available? i)
                                     (button/button {:label "Vender" :kind :secondary :size :sm :type "button"
                                                     :on-click #(rf/dispatch [:stock/open-sale i])}))
                                   (button/button {:label "Editar" :kind :ghost :size :sm :type "button"
                                                   :on-click #(rf/dispatch [:stock/open-edit i])})])))))])
                (pagination/pagination
                 {:page page :total-pages total-pages :total total :per-page per-page
                  :on-page-change #(rf/dispatch [:stock/set-page %])}))
     (item-drawer)
     (movement-drawer))))
