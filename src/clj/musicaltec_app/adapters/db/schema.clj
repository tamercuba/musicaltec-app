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
    (map? schema)                 :db.type/ref
    :else (throw (ex-info "Tipo de schema sem mapeamento para Datomic"
                          {:schema schema}))))

(defn- ->attribute [ident value-schema]
  (let [unique?    (db.schema/unique? value-schema)
        component? (db.schema/component? value-schema)
        schema     (cond
                     unique?    (db.schema/unique-schema value-schema)
                     component? (db.schema/component-schema value-schema)
                     :else      value-schema)
        many?      (vector? schema)
        elem       (if many? (first schema) schema)]
    (cond-> {:db/ident       ident
             :db/valueType   (if component? :db.type/ref (->value-type elem))
             :db/cardinality (if many? :db.cardinality/many :db.cardinality/one)}
      unique?    (assoc :db/unique (get unique-mode->datomic
                                        (db.schema/unique-mode value-schema)))
      component? (assoc :db/isComponent true))))

(defn- ->nested-attributes [value-schema]
  (when (db.schema/component? value-schema)
    (let [inner  (db.schema/component-schema value-schema)
          schema (if (vector? inner) (first inner) inner)]
      (->> schema
           (map (fn [[k v]] (->attribute (s/explicit-schema-key k) v)))
           vec))))

(s/defn db-schema->datomic :- [s/Any]
  [db-schema :- s/Any]
  (->> (concat
        (map (fn [[k v]] (->attribute (s/explicit-schema-key k) v)) db-schema)
        (mapcat ->nested-attributes (vals db-schema)))
       (sort-by :db/ident)
       vec))
