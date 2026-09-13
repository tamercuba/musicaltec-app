(ns musicaltec-app.domain.models.product
  (:require [schema.core :as s]))

(def product-kind #{:instrument :accessory})

(s/defschema ProductKind (apply s/enum product-kind))

(s/defschema Product
  #:product{:id     s/Uuid
            :name   s/Str
            :brand  s/Str
            :attrs  s/Str
            :amount s/Int
            :kind   ProductKind})

