(ns musicaltec-app.domain.logic.search
  (:require [clojure.string :as str]
            [schema.core :as s]))

(s/defn normalize :- s/Str
  [s :- (s/maybe s/Str)]
  (-> s
      (or "")
      str/trim
      str/lower-case
      (java.text.Normalizer/normalize java.text.Normalizer$Form/NFD)
      (str/replace #"\p{M}" "")
      (str/replace #"\s+" " ")))
