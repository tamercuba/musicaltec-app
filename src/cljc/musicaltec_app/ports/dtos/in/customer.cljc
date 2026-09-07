(ns musicaltec-app.ports.dtos.in.customer
  (:require [clojure.string :as str]
            [schema.core :as s]
            [musicaltec-app.ports.value :as value]))

(def ^:private CreateCustomerFields
  {:name                   (s/constrained s/Str (complement str/blank?))
   :phones                 [value/Phone]
   (s/optional-key :email) (s/constrained s/Str (complement str/blank?))
   :kind                   (s/enum :person :company)
   :tax-id                 value/TaxId})

(defn- kind-matches-tax-id? [{:keys [kind tax-id]}]
  (value/tax-id-matches-kind? kind tax-id))

(s/defschema CreateCustomerIn
  (s/constrained CreateCustomerFields kind-matches-tax-id?))

(s/defschema UpdateCustomerIn
  (s/constrained (merge CreateCustomerFields {:id s/Uuid}) kind-matches-tax-id?))

(s/defschema ListCustomersIn
  {(s/optional-key :q)        s/Str
   (s/optional-key :page)     s/Int
   (s/optional-key :per-page) s/Int})
