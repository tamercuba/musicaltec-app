(ns musicaltec-app.unit.adapters.db.schema-test
  (:require [clojure.test :refer [deftest is]]
            [musicaltec-app.adapters.db.schema :as db.schema]
            [musicaltec-app.ports.dtos.db.customer :as dto.db.customer]))

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
