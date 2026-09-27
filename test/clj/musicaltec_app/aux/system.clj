(ns musicaltec-app.aux.system
  (:require [datomic.api :as d]
            [musicaltec-app.adapters.api.router :as api.router]
            [musicaltec-app.adapters.db.core :as db.core]
            [musicaltec-app.adapters.db.customer :as db.customer]
            [musicaltec-app.adapters.db.financial-movement :as db.financial-movement]
            [musicaltec-app.adapters.db.stock-item :as db.stock-item]
            [musicaltec-app.adapters.db.registry :as db.registry]
            [musicaltec-app.adapters.logs.writer :as logs]))

(def password "test-password")

(defn new-context
  "Creates a full HTTP handler over an empty in-memory Datomic database."
  []
  (let [conn (db.core/connect (str "datomic:mem://test-" (random-uuid)) db.registry/schemas)]
    {:conn    conn
     :handler (api.router/handler
               (api.router/router
                {:db/customer-repo            (db.customer/->repository conn)
                 :db/stock-item-repo          (db.stock-item/->repository conn)
                 :db/financial-movement-repo  (db.financial-movement/->repository conn)
                 :log/logger                  (logs/->writer-logger System/out)}
                {:password password}))}))

(defn close! [ctx]
  (d/release (:conn ctx)))
