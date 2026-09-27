(ns musicaltec-app.domain.mappers.pagination
  (:require [musicaltec-app.domain.models.customer :as models.customer]
            [musicaltec-app.domain.models.pagination :as models.pagination]
            [musicaltec-app.ports.dtos.in.customer :as dtos.in.customer]
            [musicaltec-app.ports.dtos.out.customer :as dtos.out.customer]
            [schema.core :as s]))

(def ^:private default-per-page 10)

(defn- parse-long-or [s default]
  (if (and s (re-matches #"\d+" s))
    (Long/parseLong s)
    default))

(s/defn dto->model :- models.pagination/Query
  [{:keys [q page per-page]} :- dtos.in.customer/ListCustomersIn]
  {:query    (or q "")
   :page     (parse-long-or page 1)
   :per-page (parse-long-or per-page default-per-page)})

(s/defn query->model :- models.pagination/Query
  "Generic query DTO -> Query model (for any list endpoint with q/page/per-page)."
  [{:keys [q page per-page]} :- s/Any]
  {:query    (or q "")
   :page     (parse-long-or page 1)
   :per-page (parse-long-or per-page default-per-page)})

(s/defn paginated->dto :- s/Any
  "Generic paginated model -> list DTO, mapping each item with `item->dto`."
  [paginated :- s/Any
   item->dto :- (s/=> s/Any s/Any)]
  (->> paginated
       :items
       (mapv item->dto)
       (assoc paginated :items)))

(s/defn model->dto :- dtos.out.customer/ListCustomersOut
  [paginated :- models.customer/PaginatedCustomer
   item->dto :- (s/=> dtos.out.customer/CustomerOut models.customer/Customer)]
  (->> paginated
       :items
       (mapv item->dto)
       (assoc paginated :items)))

