(ns musicaltec-app.unit.domain.mappers.stock-item-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.domain.mappers.stock-item :as mappers.stock-item]))

(deftest dto->model-applies-defaults
  (let [m (mappers.stock-item/dto->model {:name "Sax Alto"})]
    (is (uuid? (:stock-item/id m)))
    (is (= "Sax Alto" (:stock-item/name m)))
    (is (= 1 (:stock-item/quantity m)))
    (is (= 0 (:stock-item/cost m)))
    (is (= 0 (:stock-item/default-price m)))
    (is (true? (:stock-item/available? m)))))

(deftest dto->model-keeps-provided-values
  (let [m (mappers.stock-item/dto->model {:name "Sax Alto" :brand "Yamaha" :quantity 2 :cost 100000 :default-price 150000})]
    (is (= "Yamaha" (:stock-item/brand m)))
    (is (= 2 (:stock-item/quantity m)))
    (is (= 100000 (:stock-item/cost m)))
    (is (= 150000 (:stock-item/default-price m)))))

(deftest model->dto-roundtrip
  (let [m   (mappers.stock-item/dto->model {:name "Sax Alto" :cost 100000})
        dto (mappers.stock-item/model->dto m)]
    (is (string? (:id dto)))
    (is (= "Sax Alto" (:name dto)))
    (is (true? (:available? dto)))))
