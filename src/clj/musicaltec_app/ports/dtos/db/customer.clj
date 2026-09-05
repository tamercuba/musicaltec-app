(ns musicaltec-app.ports.dtos.db.customer
  (:require [musicaltec-app.ports.schema :as db.schema]
            [schema.core :as s]))

(s/defschema CustomerDb
  {:customer/id     (db.schema/unique s/Uuid :identity)
   :customer/name   s/Str
   :customer/phone  [s/Str]
   (s/optional-key :customer/email) (db.schema/unique s/Str :value)
   :customer/kind   s/Keyword
   :customer/tax-id (db.schema/unique s/Str :value)})
