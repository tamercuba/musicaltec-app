(ns musicaltec-app.unit.domain.mappers.customer-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.domain.mappers.customer :as mappers.customer]))

(defn- ->create-dto [& {:as overrides}]
  (merge {:name   "João da Silva"
          :phones ["11999990001"]
          :email  "joao@example.com"
          :kind   :person
          :tax-id "11144477735"}
         overrides))

(deftest dto->model-valid
  (let [model (mappers.customer/dto->model (->create-dto))]
    (is (uuid? (:customer/id model)))
    (is (= "João da Silva" (:customer/name model)))
    (is (= ["11999990001"] (:customer/phones model)))
    (is (= :person (:customer/kind model)))
    (is (= "11144477735" (:customer/tax-id model)))
    (is (= "joao@example.com" (:customer/email model)))))

(deftest update-dto->model-valid
  (let [id    #uuid "00000000-0000-0000-0000-000000000001"
        model (mappers.customer/update-dto->model (assoc (->create-dto) :id id))]
    (is (= id (:customer/id model)))
    (is (= "João da Silva" (:customer/name model)))
    (is (= :person (:customer/kind model)))))

(deftest model->dto
  (let [model (mappers.customer/dto->model (->create-dto))
        dto   (mappers.customer/model->dto model)]
    (is (string? (:id dto)))
    (is (= "João da Silva" (:name dto)))
    (is (= :person (:kind dto)))
    (is (= ["11999990001"] (:phones dto)))))
