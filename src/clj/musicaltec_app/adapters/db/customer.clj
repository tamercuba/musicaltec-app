(ns musicaltec-app.adapters.db.customer
  (:require [clojure.string :as str]
            [datomic.api :as d]
            [musicaltec-app.adapters.db.schema :as db.schema]
            [musicaltec-app.domain.errors :as errors]
            [musicaltec-app.domain.mappers.customer :as mappers.customer]
            [musicaltec-app.ports.db.customer :as ports.db.customer]
            [musicaltec-app.ports.dtos.db.customer :as dto.db.customer]
            [schema.core :as s]))

(def schema
  (db.schema/db-schema->datomic dto.db.customer/CustomerDb))

(s/defn connect :- s/Any
  [uri :- s/Str]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    @(d/transact conn schema)
    conn))

(defn- unique-conflict? [^Throwable e]
  (str/includes? (str (.getMessage e)) "Unique conflict"))

(defn- ->unique-conflict! [^Throwable e]
  (let [msg (str (.getMessage e))]
    (errors/conflict!
     (cond
       (str/includes? msg ":customer/tax-id") "Já existe um cliente com este CPF/CNPJ."
       (str/includes? msg ":customer/email")  "Já existe um cliente com este e-mail."
       :else                                  "Já existe um cliente com este valor."))))

(defn- transact! [conn tx]
  (try
    @(d/transact conn tx)
    (catch Exception e
      (if (unique-conflict? e)
        (->unique-conflict! e)
        (throw e)))))

(s/defrecord DatomicCustomerRepository [conn :- s/Any]
  ports.db.customer/CustomerRepository
  (insert! [_ customer]
    (transact! conn [(mappers.customer/model->db customer)])
    customer)

  (update! [_ customer]
    (let [id (:customer/id customer)]
      (when-not (:customer/id (d/pull (d/db conn) '[:customer/id] [:customer/id id]))
        (errors/not-found!))
      (transact! conn [(mappers.customer/model->db customer)])
      customer))

  (delete! [_ id]
    @(d/transact conn [[:db/retractEntity [:customer/id id]]])
    nil)

  (get! [_ id]
    (let [entity (d/pull (d/db conn) '[*] [:customer/id id])]
      (if (:customer/id entity)
        (mappers.customer/db->model entity)
        (errors/not-found!))))

  (find-all [_]
    (->> (d/q '[:find (pull ?e [*])
                :where [?e :customer/id]]
              (d/db conn))
         (map first)
         (map mappers.customer/db->model)
         (vec))))

(s/defn ->repository :- (s/protocol ports.db.customer/CustomerRepository)
  [conn :- s/Any]
  (map->DatomicCustomerRepository {:conn conn}))
