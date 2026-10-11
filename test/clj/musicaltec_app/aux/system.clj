(ns musicaltec-app.aux.system
  (:require [clojure.java.io :as io]
            [musicaltec-app.adapters.api.router :as api.router]
            [musicaltec-app.adapters.db.sql.core :as db.sql.core]
            [musicaltec-app.adapters.db.sql.customer :as db.sql.customer]
            [musicaltec-app.adapters.db.sql.financial-movement :as db.sql.financial-movement]
            [musicaltec-app.adapters.db.sql.stock-item :as db.sql.stock-item]
            [musicaltec-app.adapters.logs.writer :as logs]))

(def password "test-password")

(defn new-context
  "Creates a full HTTP handler over a fresh SQLite database."
  []
  (let [db-file (str (System/getProperty "java.io.tmpdir")
                     "/musicaltec-test-" (random-uuid) ".db")
        ds      (db.sql.core/->datasource (str "jdbc:sqlite:" db-file))]
    (db.sql.core/migrate! ds "migrations")
    {:db-file db-file
     :handler (api.router/handler
               (api.router/router
                {:db/customer-repo           (db.sql.customer/->repository ds)
                 :db/stock-item-repo         (db.sql.stock-item/->repository ds)
                 :db/financial-movement-repo (db.sql.financial-movement/->repository ds)
                 :log/logger                 (logs/->writer-logger System/out)}
                {:password password}))}))

(defn close! [ctx]
  (when-let [f (:db-file ctx)]
    (io/delete-file f true)))
