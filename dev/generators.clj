(ns generators
  (:require [musicaltec-app.ports.value :as value]))

(defn- random-digit [] (rand-int 10))

(defn random-cpf
  "Valid CPF (11 digits with correct check digits)."
  []
  (let [base (repeatedly 9 random-digit)
        dv1  (value/mod-11-dv base (range 10 1 -1))
        dv2  (value/mod-11-dv (concat base [dv1]) (range 11 1 -1))]
    (apply str (concat base [dv1 dv2]))))

(defn random-cnpj
  "Valid CNPJ (14 digits with correct check digits)."
  []
  (let [base (repeatedly 12 random-digit)
        dv1  (value/mod-11-dv base [5 4 3 2 9 8 7 6 5 4 3 2])
        dv2  (value/mod-11-dv (concat base [dv1]) [6 5 4 3 2 9 8 7 6 5 4 3 2])]
    (apply str (concat base [dv1 dv2]))))

(defn random-phone
  []
  (apply str (cons 1 (repeatedly 10 random-digit))))

(defn- random-tax-id [kind]
  (if (= kind :company) (random-cnpj) (random-cpf)))

(defn- random-kind [] (rand-nth [:person :company]))

(defn- random-name [] (str "Cliente " (rand-int 1000000)))

(defn- random-email [] (str "cliente-" (random-uuid) "@example.com"))

(defn generate-customer
  []
  (let [kind (random-kind)]
    {:name   (random-name)
     :phones [(random-phone)]
     :email  (random-email)
     :kind   kind
     :tax-id (random-tax-id kind)}))

(defn generate-customers
  [n]
  (mapv (fn [_] (generate-customer)) (range n)))
