(ns musicaltec-app.adapters.db.registry
  (:require [musicaltec-app.adapters.db.customer :as db.customer]))

(def schemas
  (->> (concat db.customer/schema)
       (sort-by :db/ident)
       vec))

