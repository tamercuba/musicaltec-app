(ns musicaltec-app.domain.logic.customer
  (:require
   [clojure.string :as str]
   [musicaltec-app.domain.logic.pagination :as logic.pagination]
   [musicaltec-app.domain.logic.search :as logic.search]
   [musicaltec-app.domain.models.customer :as models.customer]
   [schema.core :as s]
   [musicaltec-app.domain.models.pagination :as models.pagination]))

(s/defn ^:private matches? :- s/Bool
  [customer :- models.customer/Customer
   q :- s/Str]
  (or (not (seq q))
      (str/includes? (logic.search/normalize (:customer/name customer)) q)))

(s/defn filter-matching :- [models.customer/Customer]
  [customers       :- [models.customer/Customer]
   {:keys [query]} :- models.pagination/Query]
  (filterv #(matches? % (logic.search/normalize query)) customers))

(s/defn sort-by-name :- [models.customer/Customer]
  [customers :- [models.customer/Customer]]
  (sort-by (juxt :customer/name :customer/id) customers))

(s/defn ->paginated :- models.customer/PaginatedCustomer
  [customers                     :- [models.customer/Customer]
   {:keys [query per-page page]} :- models.pagination/Query]
  (let [total    (count customers)
        pages    (logic.pagination/total-pages total per-page)
        page     (logic.pagination/clamp-page page pages)]
    {:items       (vec (take per-page (drop (logic.pagination/offset page per-page) customers)))
     :query       query
     :page        page
     :total-pages pages
     :total       total
     :per-page    per-page}))

