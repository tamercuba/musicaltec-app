(ns musicaltec-app.domain.mappers.customer
  (:require [clojure.string :as str]
            [musicaltec-app.domain.models.customer :as models.customer]
            [musicaltec-app.ports.dtos.in.customer :as dtos.in.customer]
            [musicaltec-app.ports.dtos.out.customer :as dtos.out.customer]
            [musicaltec-app.ports.dtos.db.customer :as dtos.db.customer]
            [schema.core :as s]))

(defn- blank->nil [s]
  (some-> s str/trim not-empty))

(defn- ->model [id name phones email kind tax-id]
  (let [email (blank->nil email)]
    (cond-> {:customer/id     id
             :customer/name   (str/trim name)
             :customer/phones phones
             :customer/kind   kind
             :customer/tax-id tax-id}
      email (assoc :customer/email email))))

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
  [{:customer/keys [id name phone email kind tax-id]} :- dtos.db.customer/CustomerDb]
  (cond-> {:customer/id     id
           :customer/name   name
           :customer/phones (vec phone)
           :customer/kind   kind
           :customer/tax-id tax-id}
    email (assoc :customer/email email)))

(s/defn model->db :- dtos.db.customer/CustomerDb
  [{:customer/keys [id name phones email kind tax-id]} :- models.customer/Customer]
  (cond-> {:customer/id     id
           :customer/name   name
           :customer/phone  phones
           :customer/kind   kind
           :customer/tax-id tax-id}
    email (assoc :customer/email email)))
