(ns musicaltec-app.ports.dtos.out.customer
  (:require [schema.core :as s]))

(s/defschema CustomerOut
  {:id     s/Str
   :name   s/Str
   :phones [s/Str]
   (s/optional-key :email) s/Str
   :kind   (s/enum :person :company)
   :tax-id s/Str})

(s/defschema ListCustomersOut
  {:items       [CustomerOut]
   :query       s/Str
   :page        s/Int
   :total-pages s/Int
   :total       s/Int
   :per-page    s/Int})
