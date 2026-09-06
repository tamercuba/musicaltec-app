(ns musicaltec-app.unit.ports.value-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(deftest phone-normalizes
  (is (= "11999990001" ((value/normalizer value/Phone) "11 99999-0001")))
  (is (nil? (s/check value/Phone "11999990001")))
  (is (some? (s/check value/Phone "abc"))))

(deftest tax-id-normalizes
  (is (= "11144477735" ((value/normalizer value/TaxId) "111.444.777-35")))
  (is (nil? (s/check value/TaxId "11144477735")))
  (is (some? (s/check value/TaxId "abc"))))
