(ns fixtures
  (:require [musicaltec-app.domain.mappers.customer :as mappers.customer]
            [musicaltec-app.domain.use-cases.customer :as use-cases.customer]))

(def sample-customers
  [{:name   "Paulo Souza"
    :phones ["11 99999-0001"]
    :email  "paulo@example.com"
    :kind   "person"
    :tax-id "111.444.777-35"}
   {:name   "Ronaldo Lima"
    :phones ["11 98888-0002"]
    :kind   "person"
    :tax-id "529.982.247-25"}])

(defn seed! [adapters]
  (let [created (mapv (fn [dto]
                        (use-cases.customer/create
                         (mappers.customer/dto->model dto)
                         adapters))
                      sample-customers)]
    (println "Seeded" (count created) "customers.")
    created))
