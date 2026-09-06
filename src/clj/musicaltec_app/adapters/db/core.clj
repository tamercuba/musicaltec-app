(ns musicaltec-app.adapters.db.core
  (:require [clojure.string :as str]
            [datomic.api :as d]
            [musicaltec-app.domain.errors :as errors]
            [schema.core :as s]))

(s/defn connect :- s/Any
  [uri    :- s/Str
   schema :- [s/Any]]
  (d/create-database uri)
  (let [conn (d/connect uri)]
    @(d/transact conn schema)
    conn))

(defn- unique-conflict? [^Throwable e]
  (str/includes? (str (.getMessage e)) "Unique conflict"))

(s/defn transact! :- s/Any
  [conn        :- s/Any
   tx          :- [s/Any]
   conflict-fn :- s/Any]
  (try
    @(d/transact conn tx)
    (catch Exception e
      (if (unique-conflict? e)
        (conflict-fn e)
        (throw e)))))

(s/defn pull :- s/Any
  [conn       :- s/Any
   selector   :- s/Any
   lookup-ref :- s/Any]
  (d/pull (d/db conn) selector lookup-ref))

(s/defn get-by-id :- s/Any
  [conn       :- s/Any
   lookup-ref :- s/Any
   id-attr    :- s/Keyword
   mapper     :- s/Any]
  (let [entity (d/pull (d/db conn) '[*] lookup-ref)]
    (if (id-attr entity)
      (mapper entity)
      (errors/fail! :resource/not-found))))

(s/defn delete-by-id :- s/Any
  [conn       :- s/Any
   lookup-ref :- s/Any]
  @(d/transact conn [[:db/retractEntity lookup-ref]])
  nil)

(s/defn find-all :- s/Any
  [conn   :- s/Any
   attr   :- s/Keyword
   mapper :- s/Any]
  (->> (d/q '[:find (pull ?e [*]) :in $ ?attr :where [?e ?attr]]
            (d/db conn) attr)
       (map first)
       (map mapper)
       (vec)))
