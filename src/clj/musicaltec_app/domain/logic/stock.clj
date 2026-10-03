(ns musicaltec-app.domain.logic.stock
  (:require [musicaltec-app.domain.logic.financial-movement :as logic.financial-movement]
            [musicaltec-app.domain.models.financial-movement :as models.financial-movement]
            [musicaltec-app.domain.models.stock :as models.stock]
            [musicaltec-app.domain.models.stock-item :as models.stock-item]
            [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(s/defn items-total :- s/Int
  [items :- [models.stock/StockLineInput]]
  (reduce + 0 (map (fn [{:stock-line/keys [quantity unit-price]}] (* quantity unit-price)) items)))

(s/defn stock-available? :- s/Bool
  [item     :- models.stock-item/StockItem
   quantity :- value/NonNegativeInt]
  (>= (:stock-item/quantity item) quantity))

(s/defn new-item->stock-item :- models.stock-item/StockItem
  [{:stock-line/keys [name brand quantity unit-price default-price serial notes]} :- models.stock/NewItemLine]
  (cond-> #:stock-item{:id            (random-uuid)
                       :name          name
                       :quantity      quantity
                       :cost          unit-price
                       :default-price (or default-price 0)
                       :available?    true}
    brand  (assoc :stock-item/brand brand)
    serial (assoc :stock-item/serial serial)
    notes  (assoc :stock-item/notes notes)))

(s/defn apply-stock-effect :- models.stock-item/StockItem
  [item       :- models.stock-item/StockItem
   quantity   :- value/NonNegativeInt
   unit-price :- value/NonNegativeInt
   direction  :- models.financial-movement/Direction]
  (case direction
    :out (assoc item
                :stock-item/quantity   (+ (:stock-item/quantity item) quantity)
                :stock-item/cost       unit-price
                :stock-item/available? true)
    :in (let [remaining (- (:stock-item/quantity item) quantity)]
          (assoc item
                 :stock-item/quantity   remaining
                 :stock-item/available? (pos? remaining)))))

(s/defmethod logic.financial-movement/->new :stock :- models.financial-movement/FinancialMovement
  [{:financial-movement/keys [direction date counterparty description shared? items]} :- models.financial-movement/FinancialMovementInput
   total :- s/Int
   installments :- [models.financial-movement/Installment]
   id :- s/Uuid]
  (cond-> #:financial-movement{:id           id
                               :type         :stock
                               :direction    direction
                               :amount       total
                               :date         date
                               :shared?      shared?
                               :installments installments
                               :items        items}
    counterparty (assoc :financial-movement/counterparty counterparty)
    description  (assoc :financial-movement/description description)))
