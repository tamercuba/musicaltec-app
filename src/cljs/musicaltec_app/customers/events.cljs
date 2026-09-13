(ns musicaltec-app.customers.events
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [schema.core :as s]
            [musicaltec-app.core.format :as format]
            [musicaltec-app.customers.db :as db]
            [musicaltec-app.ports.dtos.in.customer :as dtos.in.customer]))

(defn- list-url [{:keys [query page per-page]}]
  (str "/api/customers"
       "?q=" (js/encodeURIComponent query)
       "&page=" page
       "&per-page=" per-page))

(rf/reg-event-fx
 :customers/load
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:customers :loading?] true)
    :http {:method     :get
           :url        (list-url (:customers db))
           :on-success [:customers/load-success]
           :on-failure [:customers/load-failure]}}))

(rf/reg-event-db
 :customers/load-success
 (fn [db [_ {:keys [items page total-pages total per-page]}]]
   (-> db
       (assoc-in [:customers :items] items)
       (assoc-in [:customers :page] page)
       (assoc-in [:customers :total-pages] total-pages)
       (assoc-in [:customers :total] total)
       (assoc-in [:customers :per-page] per-page)
       (assoc-in [:customers :loading?] false)
       (assoc-in [:customers :error] nil))))

(rf/reg-event-db
 :customers/load-failure
 (fn [db [_ _err]]
   (-> db
       (assoc-in [:customers :loading?] false)
       (assoc-in [:customers :error] "Não foi possível carregar os clientes."))))

(rf/reg-event-db
 :customers/set-query
 (fn [db [_ q]]
   (assoc-in db [:customers :query] q)))

(rf/reg-event-fx
 :customers/submit-search
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:customers :page] 1)
    :dispatch [:customers/load]}))

(rf/reg-event-fx
 :customers/set-page
 (fn [{:keys [db]} [_ page]]
   {:db (assoc-in db [:customers :page] page)
    :dispatch [:customers/load]}))

(rf/reg-event-db
 :customers/open-create
 (fn [db _]
   (assoc-in db [:customers :drawer]
             {:mode    :create
              :open?   true
              :id      nil
              :saving? false
              :error   nil
              :form    db/empty-form})))

(rf/reg-event-db
 :customers/open-edit
 (fn [db [_ customer]]
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
                        :tax-id (:tax-id customer)}})))

(rf/reg-event-db
 :customers/close-drawer
 (fn [db _]
   (-> db
       (assoc-in [:customers :drawer :open?] false)
       (assoc-in [:customers :drawer :mode]  nil)
       (assoc-in [:customers :drawer :error] nil))))

(rf/reg-event-db
 :customers/form-update
 (fn [db [_ field value]]
   (if (= field :tax-id)
     (assoc-in db [:customers :drawer :form :tax-id]
               (format/format-tax-id (get-in db [:customers :drawer :form :kind]) value))
     (assoc-in db [:customers :drawer :form field] value))))

(rf/reg-event-db
 :customers/form-set-kind
 (fn [db [_ kind]]
   (-> db
       (assoc-in [:customers :drawer :form :kind] kind)
       (assoc-in [:customers :drawer :form :tax-id]
                 (format/format-tax-id kind (get-in db [:customers :drawer :form :tax-id]))))))

(rf/reg-event-db
 :customers/form-set-phone
 (fn [db [_ index value]]
   (assoc-in db [:customers :drawer :form :phones index] (format/format-phone value))))

(rf/reg-event-db
 :customers/add-phone
 (fn [db _]
   (update-in db [:customers :drawer :form :phones] conj "")))

(rf/reg-event-db
 :customers/remove-phone
 (fn [db [_ index]]
   (update-in db [:customers :drawer :form :phones]
              (fn [phones]
                (let [phones (vec phones)]
                  (if (= 1 (count phones))
                    [""]
                    (vec (concat (subvec phones 0 index)
                                 (subvec phones (inc index))))))))))

(defn- form->body [{:keys [name phones email kind tax-id]}]
  (let [email (str/trim email)]
    (cond-> {:name   (str/trim name)
             :phones (mapv format/digits phones)
             :kind   kind
             :tax-id (format/alphanumeric tax-id)}
      (seq email) (assoc :email email))))

(defn- explain-errors [dto]
  (when-let [error (s/check dtos.in.customer/CreateCustomerIn dto)]
    (let [kind (:kind dto)]
      (if-not (map? error)
        [(if (= kind :company) "CNPJ não corresponde ao tipo." "CPF não corresponde ao tipo.")]
        (let [msgs (cond-> []
                     (contains? error :name)   (conj "Nome é obrigatório.")
                     (contains? error :phones) (conj "Informe ao menos um telefone válido.")
                     (contains? error :email)  (conj "E-mail inválido.")
                     (contains? error :kind)   (conj "Tipo inválido.")
                     (contains? error :tax-id) (conj (if (= kind :company) "CNPJ inválido." "CPF inválido.")))]
          (if (seq msgs)
            msgs
            [(if (= kind :company) "CNPJ não corresponde ao tipo." "CPF não corresponde ao tipo.")]))))))

(rf/reg-event-fx
 :customers/save
 (fn [{:keys [db]} _]
   (let [{:keys [mode id form]} (get-in db [:customers :drawer])
         body (form->body form)]
     (if-let [errors (explain-errors body)]
       {:db (assoc-in db [:customers :drawer :error] errors)}
       {:db (assoc-in db [:customers :drawer :saving?] true)
        :http (if (= mode :edit)
                {:method :put
                 :url (str "/api/customers/" id)
                 :body body
                 :on-success [:customers/save-success]
                 :on-failure [:customers/save-failure]}
                {:method :post
                 :url "/api/customers"
                 :body body
                 :on-success [:customers/save-success]
                 :on-failure [:customers/save-failure]})}))))

(rf/reg-event-fx
 :customers/save-success
 (fn [{:keys [db]} _]
   {:db (-> db
            (assoc-in [:customers :drawer :open?]   false)
            (assoc-in [:customers :drawer :mode]    nil)
            (assoc-in [:customers :drawer :saving?] false)
            (assoc-in [:customers :drawer :error]   nil))
    :dispatch [:customers/load]}))

(rf/reg-event-db
 :customers/save-failure
 (fn [db [_ {:keys [code]}]]
   (-> db
       (assoc-in [:customers :drawer :saving?] false)
       (assoc-in [:customers :drawer :error]
                 [(case code
                    :customer/tax-id-taken "CPF/CNPJ já cadastrado."
                    :customer/email-taken "E-mail já cadastrado."
                    "Não foi possível salvar.")]))))

(rf/reg-event-fx
 :customers/delete
 (fn [_ [_ id]]
   {:http {:method :delete
           :url (str "/api/customers/" id)
           :on-success [:customers/delete-success]
           :on-failure [:customers/delete-failure]}}))

(rf/reg-event-fx
 :customers/delete-success
 (fn [_ _]
   {:dispatch [:customers/load]}))

(rf/reg-event-db
 :customers/delete-failure
 (fn [db [_ _err]]
   (assoc-in db [:customers :error] "Não foi possível excluir o cliente.")))
