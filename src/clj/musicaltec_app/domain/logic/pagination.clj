(ns musicaltec-app.domain.logic.pagination
  (:require [schema.core :as s]))

(s/defn total-pages :- s/Int
  [total :- s/Int
   per-page :- s/Int]
  (max 1 (int (Math/ceil (/ (max 0 total) per-page)))))

(s/defn clamp-page :- s/Int
  "Normalized page number between 1 and `pages`"
  [page :- (s/maybe s/Int)
   pages :- s/Int]
  (min pages (or page 1)))

(s/defn offset :- s/Int
  [page     :- s/Int
   per-page :- s/Int]
  (* (dec page) per-page))
