(ns musicaltec-app.customers.logic
  (:require [clojure.string :as str]
            [schema.core :as s]
            [musicaltec-app.core.format :as format]
            [musicaltec-app.customers.db :as db]
            [musicaltec-app.labels :as labels]
            [musicaltec-app.ports.dtos.in.customer :as dtos.in.customer]))

(defn list-url
  [{:keys [query page per-page]}]
  (str "/api/customers"
       "?q=" (js/encodeURIComponent query)
       "&page=" page
       "&per-page=" per-page))

(defn load
  [db]
  {:db (assoc-in db [:customers :loading?] true)
   :http {:method     :get
          :url        (list-url (:customers db))
          :on-success [:customers/load-success]
          :on-failure [:customers/load-failure]}})

(defn load-success
  [db {:keys [items page total-pages total per-page]}]
  (-> db
      (assoc-in [:customers :items] items)
      (assoc-in [:customers :page] page)
      (assoc-in [:customers :total-pages] total-pages)
      (assoc-in [:customers :total] total)
      (assoc-in [:customers :per-page] per-page)
      (assoc-in [:customers :loading?] false)
      (assoc-in [:customers :error] nil)))

(defn load-failure
  [db]
  (-> db
      (assoc-in [:customers :loading?] false)
      (assoc-in [:customers :error] (labels/label :customers/load-failed))))

(defn set-query
  [db q]
  (assoc-in db [:customers :query] q))

(defn submit-search
  [db]
  {:db (assoc-in db [:customers :page] 1)
   :dispatch [:customers/load]})

(defn set-page
  [db page]
  {:db (assoc-in db [:customers :page] page)
   :dispatch [:customers/load]})

(defn open-create
  [db]
  (assoc-in db [:customers :drawer]
            {:mode    :create
             :open?   true
             :id      nil
             :saving? false
             :error   nil
             :form    db/empty-form}))

(defn open-edit
  [db customer]
  (assoc-in db [:customers :drawer]
            {:mode    :edit
             :open?   true
             :id      (:id customer)
             :saving? false
             :error   nil
             :form    {:name   (:name customer)
                       :phones (if (seq (:phones customer)) (vec (:phones customer)) [""])
                       :email  (or (:email customer) "")
                       :kind   (keyword (:kind customer))
                       :tax-id (:tax-id customer)}}))

(defn close-drawer
  [db]
  (-> db
      (assoc-in [:customers :drawer :open?] false)
      (assoc-in [:customers :drawer :mode] nil)
      (assoc-in [:customers :drawer :error] nil)))

(defn form-update
  [db field value]
  (if (= field :tax-id)
    (assoc-in db [:customers :drawer :form :tax-id]
              (format/format-tax-id (get-in db [:customers :drawer :form :kind]) value))
    (assoc-in db [:customers :drawer :form field] value)))

(defn form-set-kind
  [db kind]
  (-> db
      (assoc-in [:customers :drawer :form :kind] kind)
      (assoc-in [:customers :drawer :form :tax-id]
                (format/format-tax-id kind (get-in db [:customers :drawer :form :tax-id])))))

(defn form-set-phone
  [db index value]
  (assoc-in db [:customers :drawer :form :phones index] (format/format-phone value)))

(defn add-phone
  [db]
  (update-in db [:customers :drawer :form :phones] conj ""))

(defn remove-phone
  [db index]
  (update-in db [:customers :drawer :form :phones]
             (fn [phones]
               (let [phones (vec phones)]
                 (if (= 1 (count phones))
                   [""]
                   (vec (concat (subvec phones 0 index)
                                (subvec phones (inc index)))))))))

(defn form->body
  [{:keys [name phones email kind tax-id]}]
  (let [email (str/trim email)]
    (cond-> {:name   (str/trim name)
             :phones (mapv format/digits phones)
             :kind   kind
             :tax-id (format/alphanumeric tax-id)}
      (seq email) (assoc :email email))))

(defn explain-errors
  [dto]
  (when-let [error (s/check dtos.in.customer/CreateCustomerIn dto)]
    (let [kind         (:kind dto)
          mismatch-key (if (= kind :company)
                         :customers/tax-id-mismatch-company
                         :customers/tax-id-mismatch-person)]
      (if-not (map? error)
        [(labels/label mismatch-key)]
        (let [msgs (cond-> []
                     (contains? error :name)   (conj (labels/label :customers/name-required))
                     (contains? error :phones) (conj (labels/label :customers/phones-required))
                     (contains? error :email)  (conj (labels/label :customers/email-invalid))
                     (contains? error :kind)   (conj (labels/label :customers/kind-invalid))
                     (contains? error :tax-id) (conj (labels/label (if (= kind :company)
                                                                     :customers/cnpj-invalid
                                                                     :customers/cpf-invalid))))]
          (if (seq msgs)
            msgs
            [(labels/label mismatch-key)]))))))

(defn save
  [db]
  (let [{:keys [mode id form]} (get-in db [:customers :drawer])
        body (form->body form)]
    (if-let [errors (explain-errors body)]
      {:db (assoc-in db [:customers :drawer :error] errors)}
      {:db (assoc-in db [:customers :drawer :saving?] true)
       :http (if (= mode :edit)
               {:method     :put
                :url        (str "/api/customers/" id)
                :body       body
                :on-success [:customers/save-success]
                :on-failure [:customers/save-failure]}
               {:method     :post
                :url        "/api/customers"
                :body       body
                :on-success [:customers/save-success]
                :on-failure [:customers/save-failure]})})))

(defn save-success
  [db]
  {:db (-> db
           (assoc-in [:customers :drawer :open?] false)
           (assoc-in [:customers :drawer :mode] nil)
           (assoc-in [:customers :drawer :saving?] false)
           (assoc-in [:customers :drawer :error] nil))
   :dispatch [:customers/load]})

(defn save-failure
  [db code]
  (-> db
      (assoc-in [:customers :drawer :saving?] false)
      (assoc-in [:customers :drawer :error]
                [(or (labels/label code)
                     (labels/label :base/save-failed))])))

(defn delete
  [id]
  {:http {:method     :delete
          :url        (str "/api/customers/" id)
          :on-success [:customers/delete-success]
          :on-failure [:customers/delete-failure]}})

(defn delete-success
  []
  {:dispatch [:customers/load]})

(defn delete-failure
  [db]
  (assoc-in db [:customers :error] (labels/label :customers/delete-failed)))
