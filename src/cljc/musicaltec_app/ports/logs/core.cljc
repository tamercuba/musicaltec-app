(ns musicaltec-app.ports.logs.core
  (:require
   [schema.core :as s]))

(s/defprotocol Logger
  (log! :- s/Any
    [this
     level :- s/Keyword
     data  :- s/Any]))

(defn ->log!
  "Logs `data` at `level` and returns `data`."
  [data logger level]
  (log! logger level data)
  data)

(defn ->>log!
  "Logs `data` at `level` and returns `data`."
  [logger level data]
  (log! logger level data)
  data)
