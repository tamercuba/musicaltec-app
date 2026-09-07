(ns fixtures
  (:require [musicaltec-app.domain.models.customer :as models.customer]
            [musicaltec-app.domain.use-cases.customer :as use-cases.customer]
            [schema-generators.generators :as g]))

(defn- unique-str [prefix]
  (str prefix (random-uuid)))

;; O schema-generators gera os demais campos (name, phones, kind...).
;; Só "costuramos" aqui os campos que precisam ser únicos no banco
;; (:customer/tax-id e :customer/email usam :db.unique/value).
(defn- ensure-unique-fields [customer]
  (assoc customer
         :customer/tax-id (unique-str "tax-")
         :customer/email  (str (unique-str "cliente-") "@example.com")))

(def sample-customers
  (vec (g/sample 200
                 models.customer/Customer
                 {}                                                ; leaf-generators (padrão)
                 {models.customer/Customer (g/fmap ensure-unique-fields)})))

(defn seed! [adapters]
  (let [created (mapv (fn [model]
                        (use-cases.customer/create
                         model
                         adapters))
                      sample-customers)]
    (println "Seeded" (count created) "customers.")
    created))
