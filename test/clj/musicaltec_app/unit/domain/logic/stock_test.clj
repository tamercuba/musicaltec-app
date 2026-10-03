(ns musicaltec-app.unit.domain.logic.stock-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.domain.logic.stock :as logic.stock]))

(defn- ->stock-item [qty]
  {:stock-item/id            (random-uuid)
   :stock-item/name          "Sax Alto"
   :stock-item/quantity      qty
   :stock-item/cost          100000
   :stock-item/default-price 150000
   :stock-item/available?    (pos? qty)})

(deftest items-total
  (is (= 150000 (logic.stock/items-total [{:stock-line/stock-item-id (random-uuid) :stock-line/quantity 1 :stock-line/unit-price 100000}
                                          {:stock-line/stock-item-id (random-uuid) :stock-line/quantity 2 :stock-line/unit-price 25000}]))))

(deftest apply-stock-effect-sale
  (let [updated (logic.stock/apply-stock-effect (->stock-item 1) 1 150000 :in)]
    (is (= 0 (:stock-item/quantity updated)))
    (is (false? (:stock-item/available? updated)))))

(deftest apply-stock-effect-purchase
  (let [updated (logic.stock/apply-stock-effect (->stock-item 1) 2 120000 :out)]
    (is (= 3 (:stock-item/quantity updated)))
    (is (true? (:stock-item/available? updated)))
    (is (= 120000 (:stock-item/cost updated)))))

(deftest stock-available
  (is (true? (logic.stock/stock-available? (->stock-item 1) 1)))
  (is (false? (logic.stock/stock-available? (->stock-item 1) 2))))

(deftest new-item->stock-item
  (let [item (logic.stock/new-item->stock-item {:stock-line/name "Flauta" :stock-line/brand "Yamaha" :stock-line/quantity 1 :stock-line/unit-price 80000})]
    (is (= "Flauta" (:stock-item/name item)))
    (is (= "Yamaha" (:stock-item/brand item)))
    (is (= 1 (:stock-item/quantity item)))
    (is (= 80000 (:stock-item/cost item)))
    (is (true? (:stock-item/available? item)))))
