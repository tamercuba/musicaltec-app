(ns musicaltec-app.ports.dtos.db.customer
  (:require [schema.core :as s]))

(s/defschema CustomerDb
  {:id         s/Str
   :name       s/Str
   :email      (s/maybe s/Str)
   :kind       s/Str
   :tax-id     s/Str
   :status     s/Str
   :created-at s/Str
   :updated-at s/Str})

(s/defschema PhoneDb
  {:customer-id s/Str
   :position    s/Int
   :phone       s/Str})
