(ns musicaltec-app.domain.mappers.financial-movement
  (:require [musicaltec-app.domain.models.financial-movement :as models.financial-movement]
            [musicaltec-app.ports.dtos.in.financial-movement :as dtos.in.financial-movement]
            [musicaltec-app.ports.dtos.out.financial-movement :as dtos.out.financial-movement]
            [musicaltec-app.ports.dtos.db.financial-movement :as dtos.db.financial-movement]
            [schema.core :as s]))

(defn- ->stock-line [{:keys [stock-item-id quantity unit-price]}]
  #:stock-line{:stock-item-id stock-item-id
               :quantity      quantity
               :unit-price    unit-price})

(defn- installment-input->model [{:keys [due-date amount]}]
  #:installment{:due-date due-date
                :amount   amount})

(s/defn dto->model :- models.financial-movement/FinancialMovementInput
  [{:keys [type direction date counterparty
           description shared? installments items]} :- dtos.in.financial-movement/CreateFinancialMovementIn]
  (cond-> #:financial-movement{:type      type
                               :direction direction
                               :date      date
                               :shared?   (boolean shared?)
                               :items     (mapv ->stock-line items)}
    counterparty (assoc :financial-movement/counterparty counterparty)
    description  (assoc :financial-movement/description description)
    installments (assoc :financial-movement/installments (mapv installment-input->model installments))))

(defn- normalize-installment
  [{:installment/keys [number due-date amount status responsible paid-at]}]
  (cond-> #:installment{:number   number
                        :due-date due-date
                        :amount   amount
                        :status   status}
    responsible (assoc :installment/responsible responsible)
    paid-at     (assoc :installment/paid-at paid-at)))

(defn- ->stock-line-db [{:stock-line/keys [stock-item-id quantity unit-price]}]
  #:stock-line{:stock-item-id stock-item-id
               :quantity      quantity
               :unit-price    unit-price})

(s/defn model->db :- dtos.db.financial-movement/FinancialMovementDb
  [{:financial-movement/keys [id type direction amount date counterparty description shared? items installments]} :- models.financial-movement/FinancialMovement]
  (cond-> #:financial-movement{:id            id
                               :type          type
                               :direction     direction
                               :amount        amount
                               :date          date
                               :shared?       shared?
                               :items         (mapv ->stock-line-db items)
                               :installments  (mapv normalize-installment installments)}
    counterparty (assoc :financial-movement/counterparty counterparty)
    description  (assoc :financial-movement/description description)))

(defn- ->stock-line-model [{:stock-line/keys [stock-item-id quantity unit-price]}]
  #:stock-line{:stock-item-id stock-item-id
               :quantity      quantity
               :unit-price    unit-price})

(s/defn db->model :- models.financial-movement/FinancialMovement
  [{:financial-movement/keys [id type direction amount date counterparty description shared? items installments]} :- s/Any]
  (cond-> #:financial-movement{:id            id
                               :type          type
                               :direction     direction
                               :amount        amount
                               :date          date
                               :shared?       shared?
                               :items         (mapv ->stock-line-model items)
                               :installments  (mapv normalize-installment installments)}
    counterparty (assoc :financial-movement/counterparty counterparty)
    description  (assoc :financial-movement/description description)))

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
