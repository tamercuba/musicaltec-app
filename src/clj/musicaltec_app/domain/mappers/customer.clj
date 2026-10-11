(ns musicaltec-app.domain.mappers.customer
  (:require [musicaltec-app.domain.models.customer :as models.customer]
            [musicaltec-app.ports.db-value :as db-value]
            [musicaltec-app.ports.dtos.in.customer :as dtos.in.customer]
            [musicaltec-app.ports.dtos.out.customer :as dtos.out.customer]
            [musicaltec-app.ports.dtos.db.customer :as dtos.db.customer]
            [musicaltec-app.ports.dtos.db.status :as dtos.db.status]
            [schema.core :as s]))

(defn- ->model [id name phones email kind tax-id]
  (cond-> {:customer/id     id
           :customer/name   name
           :customer/phones phones
           :customer/kind   kind
           :customer/tax-id tax-id}
    email (assoc :customer/email email)))

(s/defn dto->model :- models.customer/Customer
  [{:keys [name phones email kind tax-id]} :- dtos.in.customer/CreateCustomerIn]
  (->model (random-uuid) name phones email kind tax-id))

(s/defn update-dto->model :- models.customer/Customer
  [{:keys [id name phones email kind tax-id]} :- dtos.in.customer/UpdateCustomerIn]
  (->model id name phones email kind tax-id))

(s/defn model->dto :- dtos.out.customer/CustomerOut
  [customer :- models.customer/Customer]
  (cond-> {:id      (str (:customer/id customer))
           :name    (:customer/name customer)
           :phones  (:customer/phones customer)
           :kind    (:customer/kind customer)
           :tax-id  (:customer/tax-id customer)}
    (:customer/email customer) (assoc :email (:customer/email customer))))

(s/defn db->model :- models.customer/Customer
  [customer :- dtos.db.customer/CustomerDb
   phones :- [dtos.db.customer/PhoneDb]]
  (let [{:keys [id name email kind tax-id]} customer]
    (cond-> {:customer/id     (db-value/str->uuid id)
             :customer/name   name
             :customer/phones (mapv :phone phones)
             :customer/kind   (db-value/str->kw kind)
             :customer/tax-id tax-id}
      email (assoc :customer/email email))))

(s/defn model->db :- dtos.db.customer/CustomerDb
  [{:customer/keys [id name email kind tax-id]} :- models.customer/Customer
   status :- dtos.db.status/StatusDb
   as-of  :- s/Str]
  {:id         (db-value/uuid->str id)
   :name       name
   :email      email
   :kind       (db-value/kw->str kind)
   :tax-id     tax-id
   :status     (db-value/kw->str status)
   :created-at as-of
   :updated-at as-of})

(s/defn model->db-phones :- [dtos.db.customer/PhoneDb]
  [{:customer/keys [id phones]} :- models.customer/Customer]
  (let [customer-id (db-value/uuid->str id)]
    (mapv (fn [i phone] {:customer-id customer-id :position i :phone phone})
          (range) phones)))
