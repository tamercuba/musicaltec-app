(ns musicaltec-app.domain.mappers.pagination
  (:require [musicaltec-app.domain.models.customer :as models.customer]
            [musicaltec-app.domain.models.pagination :as models.pagination]
            [musicaltec-app.ports.dtos.in.customer :as dtos.in.customer]
            [musicaltec-app.ports.dtos.out.customer :as dtos.out.customer]
            [schema.core :as s]))

(def ^:private default-per-page 10)

(s/defn dto->model :- models.pagination/Query
  [{:keys [q page per-page]} :- dtos.in.customer/ListCustomersIn]
  {:query    (or q "")
   :page     (or page 1)
   :per-page (or per-page default-per-page)})

(s/defn model->dto :- dtos.out.customer/ListCustomersOut
  [paginated :- models.customer/PaginatedCustomer
   item->dto :- (s/=> dtos.out.customer/CustomerOut models.customer/Customer)]
  (->> paginated
       :items
       (mapv item->dto)
       (assoc paginated :items)))

