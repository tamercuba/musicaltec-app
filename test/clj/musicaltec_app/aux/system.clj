(ns musicaltec-app.aux.system
  (:require [datomic.api :as d]
            [musicaltec-app.adapters.api.router :as api.router]
            [musicaltec-app.adapters.db.customer :as db.customer]))

(def password "test-password")

(defn new-context
  "Cria um handler HTTP completo sobre um banco Datomic em memória (vazio)."
  []
  (let [conn (db.customer/connect (str "datomic:mem://test-" (random-uuid)))]
    {:conn    conn
     :handler (api.router/handler
               (api.router/router
                {:db/customer-repo (db.customer/->repository conn)}
                {:password password}))}))

(defn close! [ctx]
  (d/release (:conn ctx)))
