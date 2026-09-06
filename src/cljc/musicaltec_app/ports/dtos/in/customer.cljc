(ns musicaltec-app.ports.dtos.in.customer
  (:require [schema.core :as s]
            [musicaltec-app.ports.value :as value]))

(s/defschema CreateCustomerIn
  {:name                   s/Str
   :phones                 [value/Phone]
   (s/optional-key :email) s/Str
   :kind                   (s/enum :person :company)
   :tax-id                 value/TaxId})

(s/defschema UpdateCustomerIn
  (merge CreateCustomerIn {:id s/Uuid}))

(s/defschema ListCustomersIn
  {(s/optional-key :q)        s/Str
   (s/optional-key :page)     s/Int
   (s/optional-key :per-page) s/Int})
