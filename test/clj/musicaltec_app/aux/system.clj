(ns musicaltec-app.aux.system
  (:require [datomic.api :as d]
            [musicaltec-app.adapters.api.router :as api.router]
            [musicaltec-app.adapters.db.core :as db.core]
            [musicaltec-app.adapters.db.customer :as db.customer]))

(def password "test-password")

(defn new-context
  "Creates a full HTTP handler over an empty in-memory Datomic database."
  []
  (let [conn (db.core/connect (str "datomic:mem://test-" (random-uuid)) db.customer/schema)]
    {:conn    conn
     :handler (api.router/handler
               (api.router/router
                {:db/customer-repo (db.customer/->repository conn)}
                {:password password}))}))

(defn close! [ctx]
  (d/release (:conn ctx)))
