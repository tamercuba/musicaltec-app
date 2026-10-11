(ns musicaltec-app.domain.mappers.financial-movement
  (:require [musicaltec-app.domain.models.financial-movement :as models.financial-movement]
            [musicaltec-app.ports.db-value :as db-value]
            [musicaltec-app.ports.dtos.in.financial-movement :as dtos.in.financial-movement]
            [musicaltec-app.ports.dtos.out.financial-movement :as dtos.out.financial-movement]
            [musicaltec-app.ports.dtos.db.financial-movement :as dtos.db.financial-movement]
            [musicaltec-app.ports.dtos.db.status :as dtos.db.status]
            [schema.core :as s]))

(defn- ->line [{:keys [stock-item-id name brand quantity unit-price default-price serial notes]}]
  (cond
    stock-item-id #:stock-line{:stock-item-id stock-item-id
                               :quantity      quantity
                               :unit-price    unit-price}
    name          (cond-> #:stock-line{:name       name
                                       :quantity   quantity
                                       :unit-price unit-price}
                    brand         (assoc :stock-line/brand brand)
                    default-price (assoc :stock-line/default-price default-price)
                    serial        (assoc :stock-line/serial serial)
                    notes         (assoc :stock-line/notes notes))))

(s/defn dto->model :- models.financial-movement/FinancialMovementInput
  [{:keys [type direction date counterparty
           description shared? installments-count down-payment items]} :- dtos.in.financial-movement/CreateFinancialMovementIn]
  (cond-> #:financial-movement{:type      type
                               :direction direction
                               :date      date
                               :shared?   (boolean shared?)
                               :items     (mapv ->line items)}
    counterparty       (assoc :financial-movement/counterparty counterparty)
    description        (assoc :financial-movement/description description)
    installments-count (assoc :financial-movement/installments-count installments-count)
    down-payment       (assoc :financial-movement/down-payment down-payment)))

(defn- line->db [movement-id position {:stock-line/keys [stock-item-id quantity unit-price]}]
  {:movement-id   movement-id
   :position      position
   :stock-item-id (db-value/uuid->str stock-item-id)
   :quantity      quantity
   :unit-price    unit-price})

(defn- line->model [{:keys [stock-item-id quantity unit-price]}]
  #:stock-line{:stock-item-id (db-value/str->uuid stock-item-id)
               :quantity      (long quantity)
               :unit-price    (long unit-price)})

(defn- installment->db [movement-id {:installment/keys [number due-date amount status responsible paid-at]}]
  {:movement-id movement-id
   :number      number
   :due-date    (db-value/inst->str due-date)
   :amount      amount
   :status      (db-value/kw->str status)
   :responsible (db-value/uuid->str responsible)
   :paid-at     (db-value/inst->str paid-at)})

(defn- installment->model [{:keys [number due-date amount status responsible paid-at]}]
  (cond-> #:installment{:number   (long number)
                        :due-date (db-value/str->inst due-date)
                        :amount   (long amount)
                        :status   (db-value/str->kw status)}
    responsible (assoc :installment/responsible (db-value/str->uuid responsible))
    paid-at     (assoc :installment/paid-at (db-value/str->inst paid-at))))

(s/defn model->db :- dtos.db.financial-movement/FinancialMovementDb
  [{:financial-movement/keys [id type direction amount date counterparty description shared?]} :- models.financial-movement/FinancialMovement
   status :- dtos.db.status/StatusDb
   as-of  :- s/Str]
  {:id           (db-value/uuid->str id)
   :type         (db-value/kw->str type)
   :direction    (db-value/kw->str direction)
   :amount       amount
   :date         (db-value/inst->str date)
   :shared       (db-value/bool->int shared?)
   :status       (db-value/kw->str status)
   :counterparty counterparty
   :description  description
   :created-at   as-of
   :updated-at   as-of})

(s/defn model->db-items :- [dtos.db.financial-movement/StockLineDb]
  [{:financial-movement/keys [id items]} :- models.financial-movement/FinancialMovement]
  (mapv (fn [i line] (-> id
                         db-value/uuid->str
                         (line->db i line)))
        (range) items))

(s/defn model->db-installments :- [dtos.db.financial-movement/InstallmentDb]
  [{:financial-movement/keys [id installments]} :- models.financial-movement/FinancialMovement]
  (mapv #(-> id
             db-value/uuid->str
             (installment->db %))
        installments))

(s/defn db->model :- models.financial-movement/FinancialMovement
  [movement :- dtos.db.financial-movement/FinancialMovementDb
   items :- [dtos.db.financial-movement/StockLineDb]
   installments :- [dtos.db.financial-movement/InstallmentDb]]
  (let [{:keys [id type direction amount date counterparty description shared]} movement]
    (cond-> #:financial-movement{:id           (db-value/str->uuid id)
                                 :type         (db-value/str->kw type)
                                 :direction    (db-value/str->kw direction)
                                 :amount       (long amount)
                                 :date         (db-value/str->inst date)
                                 :shared?      (db-value/int->bool shared)
                                 :items        (mapv line->model items)
                                 :installments (mapv installment->model installments)}
      counterparty (assoc :financial-movement/counterparty counterparty)
      description  (assoc :financial-movement/description description))))

(defn- ->stock-line-dto [{:stock-line/keys [stock-item-id quantity unit-price]}]
  {:stock-item-id (str stock-item-id)
   :quantity      quantity
   :unit-price    unit-price})

(defn- installment->dto [{:installment/keys [number due-date amount status responsible paid-at]}]
  (cond-> {:number   number
           :due-date due-date
           :amount   amount
           :status   status}
    responsible (assoc :responsible (str responsible))
    paid-at     (assoc :paid-at paid-at)))

(s/defn model->dto :- dtos.out.financial-movement/FinancialMovementOut
  [{:financial-movement/keys [id type direction amount date counterparty description shared? items installments]} :- models.financial-movement/FinancialMovement]
  (cond-> {:id            (str id)
           :type          type
           :direction     direction
           :amount        amount
           :date          date
           :shared?       shared?
           :installments  (mapv installment->dto installments)}
    (seq items)   (assoc :items (mapv ->stock-line-dto items))
    counterparty (assoc :counterparty counterparty)
    description  (assoc :description description)))
