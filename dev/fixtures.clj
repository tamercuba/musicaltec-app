(ns fixtures
  (:require [musicaltec-app.domain.mappers.customer :as mappers.customer]
            [musicaltec-app.domain.use-cases.customer :as use-cases.customer]))

(def sample-customers
  [{:name   "Paulo Souza"
    :phones ["11999990001"]
    :email  "paulo@example.com"
    :kind   :person
    :tax-id "11144477735"}
   {:name   "Ronaldo Lima"
    :phones ["11988880002"]
    :kind   :person
    :tax-id "52998224725"}])

(defn seed! [adapters]
  (let [created (mapv (fn [dto]
                        (use-cases.customer/create
                         (mappers.customer/dto->model dto)
                         adapters))
                      sample-customers)]
    (println "Seeded" (count created) "customers.")
    created))
