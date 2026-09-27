(ns musicaltec-app.ports.system
  (:require [musicaltec-app.ports.db.customer :as ports.db.customer]
            [musicaltec-app.ports.db.financial-movement :as ports.db.financial-movement]
            [musicaltec-app.ports.db.stock-item :as ports.db.stock-item]
            [musicaltec-app.ports.logs.core :as ports.logs.core]
            [schema.core :as s]))

(s/defschema Adapters
  {:db/customer-repo            (s/protocol ports.db.customer/CustomerRepository)
   :db/stock-item-repo          (s/protocol ports.db.stock-item/StockItemRepository)
   :db/financial-movement-repo  (s/protocol ports.db.financial-movement/FinancialMovementRepository)
   :log/logger                  (s/protocol ports.logs.core/Logger)})
