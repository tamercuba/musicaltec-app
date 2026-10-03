(ns musicaltec-app.domain.mappers.stock-item
  (:require [musicaltec-app.domain.mappers.pagination :as mappers.pagination]
            [musicaltec-app.domain.models.pagination :as models.pagination]
            [musicaltec-app.domain.models.stock-item :as models.stock-item]
            [musicaltec-app.ports.dtos.in.stock-item :as dtos.in.stock-item]
            [musicaltec-app.ports.dtos.out.stock-item :as dtos.out.stock-item]
            [musicaltec-app.ports.dtos.db.stock-item :as dtos.db.stock-item]
            [schema.core :as s]))

(def ^:private default-quantity 1)
(def ^:private default-money 0)

(s/defn query->model :- models.pagination/Query
  [{:keys [available] :as dto} :- dtos.in.stock-item/ListStockItemsIn]
  (cond-> (mappers.pagination/query->model dto)
    available (assoc :available? (= available "true"))))

(defn- ->model [id name brand quantity cost default-price acquired-at available? serial notes]
  (cond-> #:stock-item{:id            id
                       :name          name
                       :quantity      (or quantity default-quantity)
                       :cost          (or cost default-money)
                       :default-price (or default-price default-money)
                       :available?    (boolean available?)}
    brand       (assoc :stock-item/brand brand)
    acquired-at (assoc :stock-item/acquired-at acquired-at)
    serial      (assoc :stock-item/serial serial)
    notes       (assoc :stock-item/notes notes)))

(s/defn dto->model :- models.stock-item/StockItem
  [{:keys [name brand quantity cost default-price serial notes]} :- dtos.in.stock-item/CreateStockItemIn]
  (->model (random-uuid) name brand quantity cost default-price nil true serial notes))

(s/defn update-dto->model :- models.stock-item/StockItem
  [{:keys [id name brand quantity cost default-price serial notes]} :- dtos.in.stock-item/UpdateStockItemIn]
  (->model id name brand quantity cost default-price nil (pos? (or quantity default-quantity)) serial notes))

(s/defn model->dto :- dtos.out.stock-item/StockItemOut
  [{:stock-item/keys [id name brand quantity cost default-price acquired-at available? serial notes]} :- models.stock-item/StockItem]
  (cond-> {:id            (str id)
           :name          name
           :quantity      quantity
           :cost          cost
           :default-price default-price
           :available?    available?}
    brand       (assoc :brand brand)
    acquired-at (assoc :acquired-at acquired-at)
    serial      (assoc :serial serial)
    notes       (assoc :notes notes)))

(s/defn db->model :- models.stock-item/StockItem
  [{:stock-item/keys [id name brand quantity cost default-price acquired-at available? serial notes]} :- dtos.db.stock-item/StockItemDb]
  (cond-> #:stock-item{:id            id
                       :name          name
                       :quantity      quantity
                       :cost          cost
                       :default-price default-price
                       :available?    available?}
    brand       (assoc :stock-item/brand brand)
    acquired-at (assoc :stock-item/acquired-at acquired-at)
    serial      (assoc :stock-item/serial serial)
    notes       (assoc :stock-item/notes notes)))

(s/defn model->db :- dtos.db.stock-item/StockItemDb
  [{:stock-item/keys [id name brand quantity cost default-price acquired-at available? serial notes]} :- models.stock-item/StockItem]
  (cond-> #:stock-item{:id            id
                       :name          name
                       :quantity      quantity
                       :cost          cost
                       :default-price default-price
                       :available?    available?}
    brand       (assoc :stock-item/brand brand)
    acquired-at (assoc :stock-item/acquired-at acquired-at)
    serial      (assoc :stock-item/serial serial)
    notes       (assoc :stock-item/notes notes)))
