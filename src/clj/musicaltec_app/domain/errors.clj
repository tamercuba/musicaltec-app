(ns musicaltec-app.domain.errors
  (:require [schema.core :as s]
            [musicaltec-app.ports.dtos.out.error :as dtos.out.error]))

(s/defn fail! :- s/Any
  [code :- dtos.out.error/ErrorCode]
  (throw (ex-info (name code) {:code code})))
