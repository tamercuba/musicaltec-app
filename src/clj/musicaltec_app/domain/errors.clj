(ns musicaltec-app.domain.errors
  (:require [schema.core :as s]))

(s/defn not-found! :- s/Any
  []
  (throw (ex-info "Not found" {:type ::not-found})))

(s/defn conflict! :- s/Any
  [message :- (s/maybe s/Str)]
  (throw (ex-info (or message "Conflict") {:type ::conflict :message message})))
