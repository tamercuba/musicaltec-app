(ns musicaltec-app.domain.models.customer
  (:require [schema.core :as s]))

(def person-kind #{:person :company})

(s/defschema PersonKind (s/enum apply person-kind))

(s/defschema Customer
  {:customer/id                     s/Uuid
   :customer/name                   s/Str
   :customer/phones                 [s/Str]
   (s/optional-key :customer/email) s/Str
   :customer/kind                   PersonKind
   :customer/tax-id                 s/Str})

(s/defschema PaginatedCustomer
  {:items       [Customer]
   :query       s/Str
   :page        s/Int
   :total-pages s/Int
   :total       s/Int
   :per-page    s/Int})
