(ns musicaltec-app.adapters.db.schema
  (:require [musicaltec-app.ports.schema :as db.schema]
            [schema.core :as s]))

(def ^:private unique-mode->datomic
  {:identity :db.unique/identity
   :value    :db.unique/value})

(defn- ->value-type [schema]
  (cond
    (identical? schema s/Uuid)    :db.type/uuid
    (identical? schema s/Str)     :db.type/string
    (identical? schema s/Int)     :db.type/long
    (identical? schema s/Bool)    :db.type/boolean
    (identical? schema s/Keyword) :db.type/keyword
    (identical? schema s/Inst)    :db.type/instant
    :else (throw (ex-info "Tipo de schema sem mapeamento para Datomic"
                          {:schema schema}))))

(defn- ->attribute [ident value-schema]
  (let [unique? (db.schema/unique? value-schema)
        schema  (if unique? (db.schema/unique-schema value-schema) value-schema)
        many?   (vector? schema)]
    (cond-> {:db/ident       ident
             :db/valueType   (->value-type (if many? (first schema) schema))
             :db/cardinality (if many? :db.cardinality/many :db.cardinality/one)}
      unique? (assoc :db/unique (get unique-mode->datomic
                                     (db.schema/unique-mode value-schema))))))

(s/defn db-schema->datomic :- [s/Any]
  [db-schema :- s/Any]
  (->> db-schema
       (map (fn [[k v]] (->attribute (s/explicit-schema-key k) v)))
       (sort-by :db/ident)
       vec))
