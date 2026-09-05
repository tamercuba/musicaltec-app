(ns musicaltec-app.ports.schema
  (:require [schema.core :as s]))

(s/defrecord Unique [schema :- s/Any
                     mode :- s/Keyword]
  s/Schema
  (spec [_] (s/spec schema))
  (explain [_] (list 'unique (s/explain schema) mode)))

(s/defn unique :- Unique
  [schema :- s/Any
   mode :- s/Keyword]
  (assert (contains? #{:identity :value} mode)
          "unique mode deve ser :identity ou :value")
  (Unique. schema mode))

(s/defn unique? :- s/Bool
  [x :- s/Any]
  (instance? Unique x))

(s/defn unique-schema :- s/Any
  [x :- Unique]
  (:schema x))

(s/defn unique-mode :- s/Keyword
  [x :- Unique]
  (:mode x))
