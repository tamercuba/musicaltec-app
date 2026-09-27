(ns musicaltec-app.unit.adapters.db.schema-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.adapters.db.schema :as db.schema]
            [musicaltec-app.ports.dtos.db.customer :as dto.db.customer]
            [musicaltec-app.ports.schema :as ports.schema]
            [schema.core :as s]))

(deftest derives-datomic-schema
  (is (= [{:db/ident       :customer/email
           :db/valueType   :db.type/string
           :db/cardinality :db.cardinality/one
           :db/unique      :db.unique/value}
          {:db/ident       :customer/id
           :db/valueType   :db.type/uuid
           :db/cardinality :db.cardinality/one
           :db/unique      :db.unique/identity}
          {:db/ident       :customer/kind
           :db/valueType   :db.type/keyword
           :db/cardinality :db.cardinality/one}
          {:db/ident       :customer/name
           :db/valueType   :db.type/string
           :db/cardinality :db.cardinality/one}
          {:db/ident       :customer/phone
           :db/valueType   :db.type/string
           :db/cardinality :db.cardinality/many}
          {:db/ident       :customer/tax-id
           :db/valueType   :db.type/string
           :db/cardinality :db.cardinality/one
           :db/unique      :db.unique/value}]
         (db.schema/db-schema->datomic dto.db.customer/CustomerDb))))

(deftest derives-component-ref-schema
  (is (= [{:db/ident       :order/id
           :db/valueType   :db.type/uuid
           :db/cardinality :db.cardinality/one
           :db/unique      :db.unique/identity}
          {:db/ident       :order/items
           :db/valueType   :db.type/ref
           :db/cardinality :db.cardinality/many
           :db/isComponent true}
          {:db/ident       :order-item/quantity
           :db/valueType   :db.type/long
           :db/cardinality :db.cardinality/one}]
         (db.schema/db-schema->datomic
          {:order/id    (ports.schema/unique s/Uuid :identity)
           :order/items (ports.schema/component [{:order-item/quantity s/Int}])}))))
