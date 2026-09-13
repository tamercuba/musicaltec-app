(ns fixtures
  (:require [schema.core :as s]
            [generators :as generators]
            [musicaltec-app.domain.mappers.customer :as mappers.customer]
            [musicaltec-app.domain.use-cases.customer :as use-cases.customer]
            [musicaltec-app.ports.dtos.in.customer :as dtos.in.customer]))

(def ^:private max-name-length 100)
(def ^:private max-email-length 100)

(defn- bounded [s limit]
  (if (> (count s) limit)
    (subs s 0 limit)
    s))

(defn- generate [{:keys [name email] :as dto}]
  (cond-> dto
    name  (update :name  bounded max-name-length)
    email (update :email bounded max-email-length)))

(def sample-customers
  [{:name   "Paulo Souza"
    :phones ["11999990001"]
    :email  "paulo@example.com"
    :kind   :person
    :tax-id "11144477735"}
   {:name   "Ronaldo Nazario"
    :phones ["11988880002"]
    :kind   :person
    :tax-id "52998224725"}])

(defn- create! [adapters dto]
  (-> (s/validate dtos.in.customer/CreateCustomerIn dto)
      mappers.customer/dto->model
      (use-cases.customer/create adapters)))

(defn seed!
  "Seeds the fixed sample customers."
  [adapters]
  (let [created (mapv #(create! adapters (generate %)) sample-customers)]
    (println "Seeded" (count created) "customers.")
    created))

(defn seed-many!
  "Seeds `n` random valid customers."
  [n adapters]
  (let [created (mapv #(create! adapters %) (generators/generate-customers n))]
    (println "Seeded" (count created) "customers.")
    created))
