(ns musicaltec-app.events
  (:require [clojure.string :as str]
            [re-frame.core :as rf]
            [reitit.frontend.easy :as rfe]
            [musicaltec-app.db :as db]
            [musicaltec-app.format :as format]
            [musicaltec-app.storage :as storage]
            [musicaltec-app.theme :as theme]))

;; ---------------------------------------------------------------------------
;; efeitos locais
;; ---------------------------------------------------------------------------

(rf/reg-fx
 :navigate
 (fn [route-name]
   (rfe/push-state route-name)))

;; ---------------------------------------------------------------------------
;; init
;; ---------------------------------------------------------------------------

(rf/reg-event-fx
 :app/init
 (fn [_ _]
   (let [token (storage/get-item :csrf-token)]
     {:db (-> db/default-db
              (assoc-in [:auth :csrf-token] token)
              (assoc-in [:auth :logged-in?] (some? token))
              (assoc-in [:theme :dark?] (theme/current-dark?)))})))

;; ---------------------------------------------------------------------------
;; router
;; ---------------------------------------------------------------------------

(rf/reg-event-fx
 :router/navigate
 (fn [{:keys [db]} [_ match]]
   (let [route {:name (get-in match [:data :name])
                :path-params (get match :path-params {})
                :query-params (get match :query-params {})}
         fx {:db (assoc db :route route)}]
     (if (and (= (:name route) :customers)
              (get-in db [:auth :logged-in?]))
       (assoc fx :dispatch [:customers/load])
       fx))))

;; ---------------------------------------------------------------------------
;; tema
;; ---------------------------------------------------------------------------

(rf/reg-event-db
 :theme/toggle
 (fn [db _]
   (assoc-in db [:theme :dark?] (theme/toggle!))))

;; ---------------------------------------------------------------------------
;; auth
;; ---------------------------------------------------------------------------

(rf/reg-event-fx
 :auth/login
 (fn [{:keys [db]} [_ password]]
   {:db (-> db
            (assoc-in [:auth :logging-in?] true)
            (assoc-in [:auth :login-error] nil))
    :http {:method :post
           :url "/api/login"
           :basic-password password
           :on-success [:auth/login-success]
           :on-failure [:auth/login-failure]}}))

(rf/reg-event-fx
 :auth/login-success
 (fn [{:keys [db]} [_ {:keys [csrf-token]}]]
   (storage/put! :csrf-token csrf-token)
   {:db (-> db
            (assoc-in [:auth :logged-in?] true)
            (assoc-in [:auth :logging-in?] false)
            (assoc-in [:auth :csrf-token] csrf-token)
            (assoc-in [:auth :login-error] nil))
    :navigate :customers}))

(rf/reg-event-db
 :auth/login-failure
 (fn [db [_ {:keys [code]}]]
   (-> db
       (assoc-in [:auth :logging-in?] false)
       (assoc-in [:auth :login-error]
                 (case code
                   :auth/invalid-credentials "Senha incorreta."
                   "Não foi possível entrar.")))))

(rf/reg-event-fx
 :auth/logout
 (fn [{:keys [db]} _]
   (storage/del! :csrf-token)
   {:db (-> db
            (assoc-in [:auth :logged-in?] false)
            (assoc-in [:auth :csrf-token] nil)
            (assoc-in [:auth :login-error] nil))
    :navigate :login}))

(rf/reg-event-fx
 :auth/expired
 (fn [{:keys [db]} _]
   (storage/del! :csrf-token)
   {:db (-> db
            (assoc-in [:auth :logged-in?] false)
            (assoc-in [:auth :csrf-token] nil))
    :navigate :login}))

;; ---------------------------------------------------------------------------
;; customers — listagem
;; ---------------------------------------------------------------------------

(defn- list-url [{:keys [query page per-page]}]
  (str "/api/customers"
       "?q=" (js/encodeURIComponent query)
       "&page=" page
       "&per-page=" per-page))

(rf/reg-event-fx
 :customers/load
 (fn [{:keys [db]} _]
   {:db (assoc-in db [:customers :loading?] true)
    :http {:method :get
           :url (list-url (:customers db))
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

;; ---------------------------------------------------------------------------
;; customers — drawer (criação/edição)
;; ---------------------------------------------------------------------------

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
                        :kind   (:kind customer)
                        :tax-id (:tax-id customer)}})))

(rf/reg-event-db
 :customers/close-drawer
 (fn [db _]
   (-> db
       (assoc-in [:customers :drawer :open?] false)
       (assoc-in [:customers :drawer :mode] nil)
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

(rf/reg-event-fx
 :customers/save
 (fn [{:keys [db]} _]
   (let [{:keys [mode id form]} (get-in db [:customers :drawer])]
     {:db (assoc-in db [:customers :drawer :saving?] true)
      :http (if (= mode :edit)
              {:method :put
               :url (str "/api/customers/" id)
               :body (form->body form)
               :on-success [:customers/save-success]
               :on-failure [:customers/save-failure]}
              {:method :post
               :url "/api/customers"
               :body (form->body form)
               :on-success [:customers/save-success]
               :on-failure [:customers/save-failure]})})))

(rf/reg-event-fx
 :customers/save-success
 (fn [{:keys [db]} _]
   {:db (-> db
            (assoc-in [:customers :drawer :open?] false)
            (assoc-in [:customers :drawer :mode] nil)
            (assoc-in [:customers :drawer :saving?] false)
            (assoc-in [:customers :drawer :error] nil))
    :dispatch [:customers/load]}))

(rf/reg-event-db
 :customers/save-failure
 (fn [db [_ {:keys [code]}]]
   (-> db
       (assoc-in [:customers :drawer :saving?] false)
       (assoc-in [:customers :drawer :error]
                 (case code
                   :customer/tax-id-taken "CPF/CNPJ já cadastrado."
                   :customer/email-taken "E-mail já cadastrado."
                   "Não foi possível salvar.")))))

;; ---------------------------------------------------------------------------
;; customers — exclusão
;; ---------------------------------------------------------------------------

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
