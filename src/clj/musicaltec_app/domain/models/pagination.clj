(ns musicaltec-app.domain.models.pagination
  (:require [schema.core :as s]))

(s/defschema Query
  {:query    s/Str
   :page     s/Int
   :per-page s/Int})
