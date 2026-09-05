(ns musicaltec-app.ports.dtos.in.customer
  (:require [schema.core :as s]))

(s/defschema CreateCustomerIn
  {:name   s/Str
   :phones [s/Str]
   (s/optional-key :email) s/Str
   :kind   (s/enum "person" "company")
   :tax-id s/Str})

(s/defschema UpdateCustomerIn
  (merge CreateCustomerIn {:id s/Uuid}))

(s/defschema ListCustomersIn
  {(s/optional-key :q) s/Str
   (s/optional-key :page) s/Int
   (s/optional-key :per-page) s/Int})
