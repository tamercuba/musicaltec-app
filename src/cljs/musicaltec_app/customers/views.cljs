(ns musicaltec-app.customers.views
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
            [musicaltec-app.core.views.layout :as layout]
            [musicaltec-app.labels :as labels]))

(defn- search-form [query]
  [:form {:on-submit (fn [e]
                       (.preventDefault e)
                       (rf/dispatch [:customers/submit-search]))
          :class "flex items-center gap-2 w-full sm:w-auto"}
   [:div {:class "w-full sm:w-64"}
    (form/icon-input
     {:icon icons/search-icon :type "search" :name "q"
      :placeholder (labels/label :customers/search-placeholder) :value query
      :on-change #(rf/dispatch [:customers/set-query (.. % -target -value)])})]
   (button/button {:label (labels/label :base/search) :type "submit" :kind :secondary})])

(defn- customer-form []
  (let [{:keys [name phones email kind tax-id]} @(rf/subscribe [:customers/drawer-form])
        saving? @(rf/subscribe [:customers/drawer-saving?])]
    [:form {:on-submit (fn [e]
                         (.preventDefault e)
                         (rf/dispatch [:customers/save]))}
     (form/label {:for "customer-name"} (labels/label :base/name))
     (form/input {:type "text" :name "name" :id "customer-name"
                  :value name
                  :on-change #(rf/dispatch [:customers/form-update :name (.. % -target -value)])
                  :required true})
     [:div {:class "h-3"}]
     (form/label {} (labels/label :customers/phones))
     [:div {:class "space-y-2"}
      (doall
       (map-indexed
        (fn [i phone]
          [:div {:key i :class "flex gap-2"}
           (form/icon-input
            {:icon icons/phone-icon :type "tel" :name "phone"
             :placeholder (labels/label :customers/phone) :value phone
             :on-change #(rf/dispatch [:customers/form-set-phone i (.. % -target -value)])})
           (button/button {:label (labels/label :customers/remove-phone) :kind :secondary :type "button"
                           :on-click #(rf/dispatch [:customers/remove-phone i])})])
        phones))]
     [:div {:class "mt-2"}
      (button/button {:label (labels/label :customers/add-phone) :kind :secondary :type "button"
                      :on-click #(rf/dispatch [:customers/add-phone])})]
     [:div {:class "h-3"}]
     (form/label {:for "customer-email"} (labels/label :customers/email))
     (form/icon-input
      {:icon icons/email-icon :type "email" :name "email" :id "customer-email"
       :placeholder (labels/label :customers/email-placeholder) :value email
       :on-change #(rf/dispatch [:customers/form-update :email (.. % -target -value)])})
     [:div {:class "h-3"}]
     (form/label {:for "customer-kind"} (labels/label :customers/kind))
     (form/select {:name "kind" :id "customer-kind"
                   :value (cljs.core/name kind)
                   :on-change #(rf/dispatch [:customers/form-set-kind (keyword (.. % -target -value))])}
                  [:option {:value "person"} (labels/label :customers/person)]
                  [:option {:value "company"} (labels/label :customers/company)])
     [:div {:class "h-3"}]
     (form/label {:for "customer-tax-id"} (if (= kind :company) (labels/label :customers/cnpj) (labels/label :customers/cpf)))
     (form/icon-input
      {:icon icons/tax-id-icon :type "text" :name "tax-id" :id "customer-tax-id"
       :value tax-id
       :placeholder (if (= kind :company) (labels/label :customers/cnpj-mask) (labels/label :customers/cpf-mask))
       :on-change #(rf/dispatch [:customers/form-update :tax-id (.. % -target -value)])})
     [:div {:class "h-4"}]
     (button/button {:label (if saving? (labels/label :base/saving) (labels/label :base/save))
                     :type  "submit"
                     :kind  :primary
                     :disabled saving?})]))

(defn- customer-drawer []
  (let [{:keys [open? mode error]} @(rf/subscribe [:customers/drawer])]
    (drawer/drawer
     {:id       "customer-drawer"
      :title    (if (= mode :edit) (labels/label :customers/edit-title) (labels/label :customers/new-title))
      :open?    open?
      :on-close #(rf/dispatch [:customers/close-drawer])
      :body     [:div
                 (when (seq error)
                   (alert/alert {:kind :danger
                                 :body (into [:div {:class "space-y-1"}]
                                             (map (fn [m] [:div m]) error))}))
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
                [:div   {:class "flex flex-col md:flex-row items-center justify-between space-y-3 md:space-y-0 md:space-x-4 p-4"}
                 [:h1   {:class "text-2xl font-semibold text-heading"} (labels/label :customers/title)]
                 [:div  {:class "flex flex-col sm:flex-row items-center gap-2 w-full md:w-auto"}
                  (search-form   query)
                  (button/button {:label (labels/label :customers/new-customer) :kind :primary :type "button"
                                  :on-click #(rf/dispatch [:customers/open-create])})]]
                (when error
                  (alert/alert {:kind :danger :body error}))
                (cond
                  loading?
                  [:div {:class "p-6 flex justify-center"} (spinner/spinner)]

                  (empty? items)
                  [:p {:class "p-6 text-body"}
                   (if (str/blank? query)
                     (labels/label :customers/none)
                     (labels/label :customers/none-found))]

                  :else
                  [:div {:class "overflow-x-auto"}
                   (table/table
                    {}
                    (table/thead
                     {}
                     (table/tr
                      {}
                      (table/th {} (labels/label :base/name))
                      (table/th {} (labels/label :customers/phone))
                      (table/th {} (labels/label :customers/email))
                      (table/th {:class "text-right"} (labels/label :base/actions))))
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
                           {:label (labels/label :base/edit) :kind :secondary :size :sm :type "button"
                            :on-click #(rf/dispatch [:customers/open-edit c])})
                          (button/button
                           {:label (labels/label :base/delete) :kind :danger :size :sm :type "button"
                            :on-click #(when (js/window.confirm (labels/label :customers/delete-confirm))
                                         (rf/dispatch [:customers/delete (:id c)]))})])))))])
                (pagination/pagination
                 {:page page :total-pages total-pages :total total :per-page per-page
                  :on-page-change #(rf/dispatch [:customers/set-page %])}))
     (customer-drawer))))
