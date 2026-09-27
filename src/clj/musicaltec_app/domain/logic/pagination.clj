(ns musicaltec-app.domain.logic.pagination
  (:require [musicaltec-app.domain.models.pagination :as models.pagination]
            [schema.core :as s]))

(s/defn total-pages :- s/Int
  [total :- s/Int
   per-page :- s/Int]
  (max 1 (int (Math/ceil (/ (max 0 total) per-page)))))

(s/defn clamp-page :- s/Int
  "Normalized page number between 1 and `pages`"
  [page :- (s/maybe s/Int)
   pages :- s/Int]
  (min pages (max 1 (or page 1))))

(s/defn offset :- s/Int
  [page     :- s/Int
   per-page :- s/Int]
  (* (dec page) per-page))

(s/defn ->paginated :- s/Any
  "Paginates `items` according to `query` (page, per-page, query string)."
  [items                      :- [s/Any]
   {:keys [query per-page page]} :- models.pagination/Query]
  (let [total (count items)
        pages (total-pages total per-page)
        page  (clamp-page page pages)]
    {:items       (vec (take per-page (drop (offset page per-page) items)))
     :query       query
     :page        page
     :total-pages pages
     :total       total
     :per-page    per-page}))
