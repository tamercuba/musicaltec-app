(ns musicaltec-app.unit.domain.logic.stock-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.domain.logic.stock :as logic.stock]))

(defn- ->stock-item [qty]
  {:stock-item/id            (random-uuid)
   :stock-item/name          "Sax Alto"
   :stock-item/quantity      qty
   :stock-item/cost          100000
   :stock-item/default-price 150000
   :stock-item/status        :in-stock})

(deftest items-total
  (is (= 150000 (logic.stock/items-total [{:stock-line/stock-item-id (random-uuid) :stock-line/quantity 1 :stock-line/unit-price 100000}
                                          {:stock-line/stock-item-id (random-uuid) :stock-line/quantity 2 :stock-line/unit-price 25000}]))))

(deftest apply-stock-effect-sale
  (let [updated (logic.stock/apply-stock-effect (->stock-item 1) 1 :in #inst "2026-01-15")]
    (is (= 0 (:stock-item/quantity updated)))
    (is (= :sold (:stock-item/status updated)))
    (is (some? (:stock-item/sold-at updated)))))

(deftest apply-stock-effect-purchase
  (let [updated (logic.stock/apply-stock-effect (->stock-item 1) 2 :out #inst "2026-01-15")]
    (is (= 3 (:stock-item/quantity updated)))
    (is (= :in-stock (:stock-item/status updated)))))

(deftest stock-available
  (is (true? (logic.stock/stock-available? (->stock-item 1) 1)))
  (is (false? (logic.stock/stock-available? (->stock-item 1) 2))))
