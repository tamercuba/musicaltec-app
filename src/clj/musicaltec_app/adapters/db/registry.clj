(ns musicaltec-app.adapters.db.registry
  (:require [musicaltec-app.adapters.db.customer :as db.customer]
            [musicaltec-app.adapters.db.financial-movement :as db.financial-movement]
            [musicaltec-app.adapters.db.stock-item :as db.stock-item]))

(def schemas
  (->> (concat db.customer/schema
               db.stock-item/schema
               db.financial-movement/schema)
       (sort-by :db/ident)
       vec))

