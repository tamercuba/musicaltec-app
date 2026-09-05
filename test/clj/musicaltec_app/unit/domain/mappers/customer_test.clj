(ns musicaltec-app.unit.domain.mappers.customer-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.domain.mappers.customer :as mappers.customer]
            [musicaltec-app.aux.customers :as aux.customers]))

(deftest dto->model-valid
  (let [model (mappers.customer/dto->model (aux.customers/->create-dto))]
    (is (uuid? (:customer/id model)))
    (is (= "João da Silva" (:customer/name model)))
    (is (= ["11999990001"] (:customer/phones model)))
    (is (= :person (:customer/kind model)))
    (is (= "11144477735" (:customer/tax-id model)))
    (is (= "joao@example.com" (:customer/email model)))))

(deftest model->dto
  (let [model (mappers.customer/dto->model (aux.customers/->create-dto))
        dto   (mappers.customer/model->dto model)]
    (is (string? (:id dto)))
    (is (= "João da Silva" (:name dto)))
    (is (= "person" (:kind dto)))
    (is (= ["11999990001"] (:phones dto)))))
