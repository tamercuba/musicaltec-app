(ns musicaltec-app.ports.value
  (:require [clojure.string :as str]
            [schema.core :as s]))

(s/defrecord ValueObject [normalize-fn :- s/Any
                          spec-schema :- s/Any
                          explain-sym :- s/Any]
  s/Schema
  (spec [_] (s/spec spec-schema))
  (explain [_] explain-sym))

(defn normalizer [vo]
  (:normalize-fn vo))

(defn value-object-matcher [schema]
  (when (instance? ValueObject schema)
    (:normalize-fn schema)))

(defn- str-normalizer [f]
  (fn [v] (if (string? v) (f v) v)))

(def Phone
  (->ValueObject
   (str-normalizer #(str/replace % #"\D" ""))
   (s/constrained s/Str #(re-matches #"\d+" %))
   'phone))

(def TaxId
  (->ValueObject
   (str-normalizer #(-> % str/upper-case (str/replace #"[^0-9A-Z]" "")))
   (s/constrained s/Str #(re-matches #"[0-9A-Z]+" %))
   'tax-id))
