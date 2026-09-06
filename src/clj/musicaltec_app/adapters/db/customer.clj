(ns musicaltec-app.adapters.db.customer
  (:require [clojure.string :as str]
            [musicaltec-app.adapters.db.core :as db.core]
            [musicaltec-app.adapters.db.schema :as db.schema]
            [musicaltec-app.domain.errors :as errors]
            [musicaltec-app.domain.mappers.customer :as mappers.customer]
            [musicaltec-app.ports.db.customer :as ports.db.customer]
            [musicaltec-app.ports.dtos.db.customer :as dto.db.customer]
            [schema.core :as s]))

(def schema
  (db.schema/db-schema->datomic dto.db.customer/CustomerDb))

(defn- ->unique-conflict! [^Throwable e]
  (let [msg (str (.getMessage e))]
    (errors/fail!
     (cond
       (str/includes? msg ":customer/tax-id") :customer/tax-id-taken
       (str/includes? msg ":customer/email")  :customer/email-taken
       :else                                  :resource/conflict))))

(s/defrecord DatomicCustomerRepository [conn :- s/Any]
  ports.db.customer/CustomerRepository
  (insert! [_ customer]
    (db.core/transact! conn [(mappers.customer/model->db customer)] ->unique-conflict!)
    customer)

  (update! [_ customer]
    (let [id (:customer/id customer)]
      (when-not (:customer/id (db.core/pull conn '[:customer/id] [:customer/id id]))
        (errors/fail! :resource/not-found))
      (db.core/transact! conn [(mappers.customer/model->db customer)] ->unique-conflict!)
      customer))

  (delete! [_ id]
    (db.core/delete-by-id conn [:customer/id id]))

  (get! [_ id]
    (db.core/get-by-id conn [:customer/id id] :customer/id mappers.customer/db->model))

  (find-all [_]
    (db.core/find-all conn :customer/id mappers.customer/db->model)))

(s/defn ->repository :- (s/protocol ports.db.customer/CustomerRepository)
  [conn :- s/Any]
  (map->DatomicCustomerRepository {:conn conn}))
