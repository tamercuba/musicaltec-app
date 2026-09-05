(ns musicaltec-app.ports.system
  (:require [musicaltec-app.ports.db.customer :as ports.db.customer]
            [schema.core :as s]))

(s/defschema Adapters
  {:db/customer-repo (s/protocol ports.db.customer/CustomerRepository)})
