(ns musicaltec-app.stock.events
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [musicaltec-app.core.format :as format]
            [musicaltec-app.stock.db :as db]))

(defn- list-url [{:keys [query page per-page tab]}]
  (str "/api/stock-items"
       "?q=" (js/encodeURIComponent query)
       "&page=" page
       "&per-page=" per-page
       "&available=" (if (= tab :sold-out) "false" "true")))

(defn- ->instant [date]
  (str date "T00:00:00.000-00:00"))

(rf/reg-event-fx
 :stock/load
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:stock :loading?] true)
    :http {:method     :get
           :url        (list-url (:stock db))
           :on-success [:stock/load-success]
           :on-failure [:stock/load-failure]}}))

(rf/reg-event-db
 :stock/load-success
 (fn [db [_ {:keys [items page total-pages total per-page]}]]
   (-> db
       (assoc-in [:stock :items] items)
       (assoc-in [:stock :page] page)
       (assoc-in [:stock :total-pages] total-pages)
       (assoc-in [:stock :total] total)
       (assoc-in [:stock :per-page] per-page)
       (assoc-in [:stock :loading?] false)
       (assoc-in [:stock :error] nil))))

(rf/reg-event-db
 :stock/load-failure
 (fn [db [_ _]]
   (-> db
       (assoc-in [:stock :loading?] false)
       (assoc-in [:stock :error] "Não foi possível carregar o estoque."))))

(rf/reg-event-fx
 :stock/set-tab
 (fn [{:keys [db]} [_ tab]]
   {:db (-> db (assoc-in [:stock :tab] tab) (assoc-in [:stock :page] 1))
    :dispatch [:stock/load]}))

(rf/reg-event-db
 :stock/set-query
 (fn [db [_ q]]
   (assoc-in db [:stock :query] q)))

(rf/reg-event-fx
 :stock/submit-search
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:stock :page] 1)
    :dispatch [:stock/load]}))

(rf/reg-event-fx
 :stock/set-page
 (fn [{:keys [db]} [_ page]]
   {:db (assoc-in db [:stock :page] page)
    :dispatch [:stock/load]}))

(rf/reg-event-db
 :stock/open-edit
 (fn [db [_ item]]
   (assoc-in db [:stock :item-drawer]
             {:mode    :edit
              :open?   true
              :id      (:id item)
              :saving? false
              :error   nil
              :form    {:name          (:name item)
                        :brand         (or (:brand item) "")
                        :quantity      (str (:quantity item))
                        :available?    (:available? item)
                        :cost          (format/cents->brl (:cost item))
                        :default-price (format/cents->brl (:default-price item))
                        :serial        (or (:serial item) "")
                        :notes         (or (:notes item) "")}})))

(rf/reg-event-db
 :stock/close-item-drawer
 (fn [db _]
   (-> db
       (assoc-in [:stock :item-drawer :open?] false)
       (assoc-in [:stock :item-drawer :mode] nil)
       (assoc-in [:stock :item-drawer :error] nil))))

(rf/reg-event-db
 :stock/item-form-update
 (fn [db [_ field value]]
   (assoc-in db [:stock :item-drawer :form field] value)))

(defn- item-form->body [{:keys [name brand quantity cost default-price serial notes]}]
  (cond-> {:name    name
           :quantity (js/parseInt (or quantity "1"))
           :cost     (or (format/brl->cents cost) 0)
           :default-price (or (format/brl->cents default-price) 0)}
    (seq brand)  (assoc :brand brand)
    (seq serial) (assoc :serial serial)
    (seq notes)  (assoc :notes notes)))

(rf/reg-event-fx
 :stock/save-item
 (fn [{:keys [db]} _]
   (let [{:keys [id form]} (get-in db [:stock :item-drawer])
         body (item-form->body form)]
     (if (str/blank? (:name form))
       {:db (assoc-in db [:stock :item-drawer :error] ["Nome é obrigatório."])}
       {:db (assoc-in db [:stock :item-drawer :saving?] true)
        :http {:method     :put
               :url        (str "/api/stock-items/" id)
               :body       body
               :on-success [:stock/save-item-success]
               :on-failure [:stock/save-item-failure]}}))))

(rf/reg-event-fx
 :stock/save-item-success
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:stock :item-drawer :open?] false)
    :dispatch [:stock/load]}))

(rf/reg-event-db
 :stock/save-item-failure
 (fn [db [_ _]]
   (-> db
       (assoc-in [:stock :item-drawer :saving?] false)
       (assoc-in [:stock :item-drawer :error] ["Não foi possível salvar."]))))

(defn- prefill [item price]
  (-> (db/empty-movement-form)
      (assoc :stock-item-id (:id item)
             :name (:name item)
             :available-quantity (:quantity item)
             :unit-price (format/cents->brl price))))

(rf/reg-event-db
 :stock/open-purchase
 (fn [db [_ item]]
   (assoc-in db [:stock :movement-drawer]
             {:mode :purchase :open? true :saving? false :error nil
              :form (prefill item (:cost item))})))

(rf/reg-event-db
 :stock/open-sale
 (fn [db [_ item]]
   (assoc-in db [:stock :movement-drawer]
             {:mode :sale :open? true :saving? false :error nil
              :form (prefill item (:default-price item))})))

(rf/reg-event-db
 :stock/open-new-item
 (fn [db _]
   (assoc-in db [:stock :movement-drawer]
             {:mode :new-item :open? true :saving? false :error nil
              :form (db/empty-movement-form)})))

(rf/reg-event-db
 :stock/close-movement-drawer
 (fn [db _]
   (-> db
       (assoc-in [:stock :movement-drawer :open?] false)
       (assoc-in [:stock :movement-drawer :mode] nil)
       (assoc-in [:stock :movement-drawer :error] nil))))

(rf/reg-event-db
 :stock/movement-form-update
 (fn [db [_ field value]]
   (assoc-in db [:stock :movement-drawer :form field] value)))

(defn- movement-form->body
  [form direction]
  (let [qty   (js/parseInt (or (:quantity form) "1"))
        price (format/brl->cents (:unit-price form))
        item  (if (:stock-item-id form)
                {:stock-item-id (:stock-item-id form) :quantity qty :unit-price price}
                (cond-> {:name (:name form) :quantity qty :unit-price price}
                  (seq (:brand form)) (assoc :brand (:brand form))
                  (seq (:default-price form)) (assoc :default-price (format/brl->cents (:default-price form)))))]
    (cond-> {:type "stock" :direction (name direction) :date (->instant (:date form)) :shared? true :items [item]}
      (seq (:counterparty form)) (assoc :counterparty (:counterparty form))
      (:parcelado? form) (assoc :installments-count (js/parseInt (:installments-count form)))
      (and (:parcelado? form) (:has-down-payment? form) (seq (:down-payment form)))
      (assoc :down-payment (format/brl->cents (:down-payment form))))))

(rf/reg-event-fx
 :stock/save-movement
 (fn [{:keys [db]} _]
   (let [{:keys [mode form]} (get-in db [:stock :movement-drawer])
         direction (if (= mode :sale) :in :out)
         qty       (js/parseInt (or (:quantity form) "1"))
         error     (cond
                     (and (nil? (:stock-item-id form)) (not (seq (:name form))))
                     ["Informe o nome do item."]

                     (and (= mode :sale) (> qty (:available-quantity form)))
                     ["Estoque insuficiente."]

                     :else nil)]
     (if error
       {:db (assoc-in db [:stock :movement-drawer :error] error)}
       {:db (assoc-in db [:stock :movement-drawer :saving?] true)
        :http {:method     :post
               :url        "/api/financial-movements"
               :body       (movement-form->body form direction)
               :on-success [:stock/save-movement-success]
               :on-failure [:stock/save-movement-failure]}}))))

(rf/reg-event-fx
 :stock/save-movement-success
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:stock :movement-drawer :open?] false)
    :dispatch [:stock/load]}))

(rf/reg-event-db
 :stock/save-movement-failure
 (fn [db [_ {:keys [code]}]]
   (-> db
       (assoc-in [:stock :movement-drawer :saving?] false)
       (assoc-in [:stock :movement-drawer :error]
                 [(case code
                    :financial-movement/stock-unavailable "Estoque insuficiente."
                    :financial-movement/down-payment-invalid "Entrada inválida."
                    "Não foi possível salvar.")]))))

(rf/reg-event-fx
 :stock/search-counterparty
 (fn [_ [_ q]]
   {:http {:method     :get
           :url        (str "/api/customers?q=" (js/encodeURIComponent q) "&per-page=10")
           :on-success [:stock/search-counterparty-success]
           :on-failure [:stock/search-counterparty-failure]}}))

(rf/reg-event-db
 :stock/search-counterparty-success
 (fn [db [_ {:keys [items]}]]
   (assoc-in db [:stock :counterparty-results] items)))

(rf/reg-event-db
 :stock/search-counterparty-failure
 (fn [db [_ _]]
   (assoc-in db [:stock :counterparty-results] [])))

(rf/reg-event-db
 :stock/clear-counterparty-results
 (fn [db _]
   (assoc-in db [:stock :counterparty-results] [])))

(rf/reg-event-db
 :stock/select-counterparty
 (fn [db [_ name]]
   (-> db
       (assoc-in [:stock :movement-drawer :form :counterparty] name)
       (assoc-in [:stock :counterparty-results] []))))
