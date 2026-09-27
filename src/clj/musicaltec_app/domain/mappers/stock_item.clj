(ns musicaltec-app.domain.mappers.stock-item
  (:require [musicaltec-app.domain.models.stock-item :as models.stock-item]
            [musicaltec-app.ports.dtos.in.stock-item :as dtos.in.stock-item]
            [musicaltec-app.ports.dtos.out.stock-item :as dtos.out.stock-item]
            [musicaltec-app.ports.dtos.db.stock-item :as dtos.db.stock-item]
            [schema.core :as s]))

(def ^:private default-quantity 1)
(def ^:private default-money 0)

(defn- ->model [id name brand quantity cost default-price acquired-at status serial notes sold-at]
  (cond-> {:stock-item/id            id
           :stock-item/name          name
           :stock-item/quantity      (or quantity default-quantity)
           :stock-item/cost          (or cost default-money)
           :stock-item/default-price (or default-price default-money)
           :stock-item/status        status}
    brand       (assoc :stock-item/brand brand)
    acquired-at (assoc :stock-item/acquired-at acquired-at)
    serial      (assoc :stock-item/serial serial)
    notes       (assoc :stock-item/notes notes)
    sold-at     (assoc :stock-item/sold-at sold-at)))

(s/defn dto->model :- models.stock-item/StockItem
  [{:keys [name brand quantity cost default-price serial notes]} :- dtos.in.stock-item/CreateStockItemIn]
  (->model (random-uuid) name brand quantity cost default-price nil :in-stock serial notes nil))

(s/defn update-dto->model :- models.stock-item/StockItem
  [{:keys [id name brand quantity cost default-price serial notes]} :- dtos.in.stock-item/UpdateStockItemIn]
  (->model id name brand quantity cost default-price nil :in-stock serial notes nil))

(s/defn model->dto :- dtos.out.stock-item/StockItemOut
  [{:stock-item/keys [id name brand quantity cost default-price acquired-at status serial notes sold-at]} :- models.stock-item/StockItem]
  (cond-> {:id            (str id)
           :name          name
           :quantity      quantity
           :cost          cost
           :default-price default-price
           :status        status}
    brand       (assoc :brand brand)
    acquired-at (assoc :acquired-at acquired-at)
    serial      (assoc :serial serial)
    notes       (assoc :notes notes)
    sold-at     (assoc :sold-at sold-at)))

(s/defn db->model :- models.stock-item/StockItem
  [{:stock-item/keys [id name brand quantity cost default-price acquired-at status serial notes sold-at]} :- dtos.db.stock-item/StockItemDb]
  (cond-> {:stock-item/id            id
           :stock-item/name          name
           :stock-item/quantity      quantity
           :stock-item/cost          cost
           :stock-item/default-price default-price
           :stock-item/status        status}
    brand       (assoc :stock-item/brand brand)
    acquired-at (assoc :stock-item/acquired-at acquired-at)
    serial      (assoc :stock-item/serial serial)
    notes       (assoc :stock-item/notes notes)
    sold-at     (assoc :stock-item/sold-at sold-at)))

(s/defn model->db :- dtos.db.stock-item/StockItemDb
  [{:stock-item/keys [id name brand quantity cost default-price acquired-at status serial notes sold-at]} :- models.stock-item/StockItem]
  (cond-> {:stock-item/id            id
           :stock-item/name          name
           :stock-item/quantity      quantity
           :stock-item/cost          cost
           :stock-item/default-price default-price
           :stock-item/status        status}
    brand       (assoc :stock-item/brand brand)
    acquired-at (assoc :stock-item/acquired-at acquired-at)
    serial      (assoc :stock-item/serial serial)
    notes       (assoc :stock-item/notes notes)
    sold-at     (assoc :stock-item/sold-at sold-at)))
