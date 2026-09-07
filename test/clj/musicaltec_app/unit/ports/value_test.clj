(ns musicaltec-app.unit.ports.value-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(deftest phone-validates
  (is (nil? (s/check value/Phone "11999990001")))
  (is (nil? (s/check value/Phone "1133334444")))
  (is (some? (s/check value/Phone "123")))
  (is (some? (s/check value/Phone "(11) 99999-0001")))
  (is (some? (s/check value/Phone "abc"))))

(deftest tax-id-validates
  (is (nil? (s/check value/TaxId "11144477735")))
  (is (nil? (s/check value/TaxId "11222333000181")))
  (is (some? (s/check value/TaxId "12345678901")))
  (is (some? (s/check value/TaxId "111.444.777-35")))
  (is (some? (s/check value/TaxId "abc"))))

(deftest person-tax-id-valid
  (is (value/person-tax-id-valid? "11144477735"))
  (is (value/person-tax-id-valid? "52998224725"))
  (is (not (value/person-tax-id-valid? "12345678901"))))

(deftest company-tax-id-valid
  (is (value/company-tax-id-valid? "11222333000181"))
  (is (not (value/company-tax-id-valid? "11222333000182"))))

(deftest tax-id-matches-kind
  (is (value/tax-id-matches-kind? :person "11144477735"))
  (is (not (value/tax-id-matches-kind? :person "11222333000181")))
  (is (value/tax-id-matches-kind? :company "11222333000181"))
  (is (not (value/tax-id-matches-kind? :company "11144477735"))))
