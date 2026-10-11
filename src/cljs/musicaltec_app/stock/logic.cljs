(ns musicaltec-app.stock.logic
  (:require [clojure.string :as str]
            [musicaltec-app.core.format :as format]
            [musicaltec-app.labels :as labels]
            [musicaltec-app.stock.db :as db]))

(defn list-url
  [{:keys [query page per-page tab]}]
  (str "/api/stock-items"
       "?q=" (js/encodeURIComponent query)
       "&page=" page
       "&per-page=" per-page
       "&available=" (if (= tab :sold-out) "false" "true")))

(defn ->instant
  [date]
  (str date "T00:00:00.000-00:00"))

(defn load
  [db]
  {:db (assoc-in db [:stock :loading?] true)
   :http {:method     :get
          :url        (list-url (:stock db))
          :on-success [:stock/load-success]
          :on-failure [:stock/load-failure]}})

(defn load-success
  [db {:keys [items page total-pages total per-page]}]
  (-> db
      (assoc-in [:stock :items] items)
      (assoc-in [:stock :page] page)
      (assoc-in [:stock :total-pages] total-pages)
      (assoc-in [:stock :total] total)
      (assoc-in [:stock :per-page] per-page)
      (assoc-in [:stock :loading?] false)
      (assoc-in [:stock :error] nil)))

(defn load-failure
  [db]
  (-> db
      (assoc-in [:stock :loading?] false)
      (assoc-in [:stock :error] (labels/label :stock/load-failed))))

(defn set-tab
  [db tab]
  {:db (-> db (assoc-in [:stock :tab] tab) (assoc-in [:stock :page] 1))
   :dispatch [:stock/load]})

(defn set-query
  [db q]
  (assoc-in db [:stock :query] q))

(defn submit-search
  [db]
  {:db (assoc-in db [:stock :page] 1)
   :dispatch [:stock/load]})

(defn set-page
  [db page]
  {:db (assoc-in db [:stock :page] page)
   :dispatch [:stock/load]})

(defn open-edit
  [db item]
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
                       :notes         (or (:notes item) "")}}))

(defn close-item-drawer
  [db]
  (-> db
      (assoc-in [:stock :item-drawer :open?] false)
      (assoc-in [:stock :item-drawer :mode] nil)
      (assoc-in [:stock :item-drawer :error] nil)))

(defn item-form-update
  [db field value]
  (assoc-in db [:stock :item-drawer :form field] value))

(defn item-form->body
  [{:keys [name brand quantity cost default-price serial notes]}]
  (cond-> {:name          name
           :quantity      (js/parseInt (or quantity "1"))
           :cost          (or (format/brl->cents cost) 0)
           :default-price (or (format/brl->cents default-price) 0)}
    (seq brand)  (assoc :brand brand)
    (seq serial) (assoc :serial serial)
    (seq notes)  (assoc :notes notes)))

(defn save-item
  [db]
  (let [{:keys [id form]} (get-in db [:stock :item-drawer])
        body (item-form->body form)]
    (if (str/blank? (:name form))
      {:db (assoc-in db [:stock :item-drawer :error] [(labels/label :stock/name-required)])}
      {:db (assoc-in db [:stock :item-drawer :saving?] true)
       :http {:method     :put
              :url        (str "/api/stock-items/" id)
              :body       body
              :on-success [:stock/save-item-success]
              :on-failure [:stock/save-item-failure]}})))

(defn save-item-success
  [db]
  {:db (assoc-in db [:stock :item-drawer :open?] false)
   :dispatch [:stock/load]})

(defn save-item-failure
  [db]
  (-> db
      (assoc-in [:stock :item-drawer :saving?] false)
      (assoc-in [:stock :item-drawer :error] [(labels/label :base/save-failed)])))

(defn prefill
  [item price]
  (-> (db/empty-movement-form)
      (assoc :stock-item-id (:id item)
             :name (:name item)
             :available-quantity (:quantity item)
             :unit-price (format/cents->brl price))))

(defn open-purchase
  [db item]
  (assoc-in db [:stock :movement-drawer]
            {:mode :purchase :open? true :saving? false :error nil
             :form (prefill item (:cost item))}))

(defn open-sale
  [db item]
  (assoc-in db [:stock :movement-drawer]
            {:mode :sale :open? true :saving? false :error nil
             :form (prefill item (:default-price item))}))

(defn open-new-item
  [db]
  (assoc-in db [:stock :movement-drawer]
            {:mode :new-item :open? true :saving? false :error nil
             :form (db/empty-movement-form)}))

(defn close-movement-drawer
  [db]
  (-> db
      (assoc-in [:stock :movement-drawer :open?] false)
      (assoc-in [:stock :movement-drawer :mode] nil)
      (assoc-in [:stock :movement-drawer :error] nil)))

(defn movement-form-update
  [db field value]
  (assoc-in db [:stock :movement-drawer :form field] value))

(defn movement-form->body
  [form direction]
  (let [qty   (js/parseInt (or (:quantity form) "1"))
        price (format/brl->cents (:unit-price form))
        item  (if (:stock-item-id form)
                {:stock-item-id (:stock-item-id form) :quantity qty :unit-price price}
                (cond-> {:name (:name form) :quantity qty :unit-price price}
                  (seq (:brand form)) (assoc :brand (:brand form))
                  (seq (:default-price form)) (assoc :default-price (format/brl->cents (:default-price form)))
                  (seq (:serial form)) (assoc :serial (:serial form))
                  (seq (:notes form)) (assoc :notes (:notes form))))]
    (cond-> {:type "stock" :direction (name direction) :date (->instant (:date form)) :shared? true :items [item]}
      (seq (:counterparty form)) (assoc :counterparty (:counterparty form))
      (:parcelado? form) (assoc :installments-count (js/parseInt (:installments-count form)))
      (and (:parcelado? form) (:has-down-payment? form) (seq (:down-payment form)))
      (assoc :down-payment (format/brl->cents (:down-payment form))))))

(defn validate-movement
  [mode form]
  (let [qty (js/parseInt (or (:quantity form) "1"))]
    (cond
      (and (nil? (:stock-item-id form)) (not (seq (:name form))))
      [(labels/label :stock/movement-name-required)]

      (and (= mode :sale) (> qty (:available-quantity form)))
      [(labels/label :financial-movement/stock-unavailable)]

      :else nil)))

(defn save-movement
  [db]
  (let [{:keys [mode form]} (get-in db [:stock :movement-drawer])
        direction (if (= mode :sale) :in :out)
        error     (validate-movement mode form)]
    (if error
      {:db (assoc-in db [:stock :movement-drawer :error] error)}
      {:db (assoc-in db [:stock :movement-drawer :saving?] true)
       :http {:method     :post
              :url        "/api/financial-movements"
              :body       (movement-form->body form direction)
              :on-success [:stock/save-movement-success]
              :on-failure [:stock/save-movement-failure]}})))

(defn save-movement-success
  [db]
  {:db (assoc-in db [:stock :movement-drawer :open?] false)
   :dispatch [:stock/load]})

(defn save-movement-failure
  [db code]
  (-> db
      (assoc-in [:stock :movement-drawer :saving?] false)
      (assoc-in [:stock :movement-drawer :error]
                [(or (labels/label code)
                     (labels/label :base/save-failed))])))

(defn search-counterparty
  [q]
  {:http {:method     :get
          :url        (str "/api/customers?q=" (js/encodeURIComponent q) "&per-page=10")
          :on-success [:stock/search-counterparty-success]
          :on-failure [:stock/search-counterparty-failure]}})

(defn search-counterparty-success
  [db {:keys [items]}]
  (assoc-in db [:stock :counterparty-results] items))

(defn search-counterparty-failure
  [db]
  (assoc-in db [:stock :counterparty-results] []))

(defn clear-counterparty-results
  [db]
  (assoc-in db [:stock :counterparty-results] []))

(defn select-counterparty
  [db name]
  (-> db
      (assoc-in [:stock :movement-drawer :form :counterparty] name)
      (assoc-in [:stock :counterparty-results] [])))
