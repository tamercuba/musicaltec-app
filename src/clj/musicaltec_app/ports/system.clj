(ns musicaltec-app.ports.system
  (:require [musicaltec-app.ports.db.customer :as ports.db.customer]
            [musicaltec-app.ports.logs.core :as ports.logs.core]
            [schema.core :as s]))

(s/defschema Adapters
  {:db/customer-repo (s/protocol ports.db.customer/CustomerRepository)
   :log/logger       (s/protocol ports.logs.core/Logger)})
