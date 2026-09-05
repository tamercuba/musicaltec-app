(ns musicaltec-app.domain.mappers.pagination
  (:require [musicaltec-app.domain.models.customer :as models.customer]
            [musicaltec-app.domain.models.pagination :as models.pagination]
            [musicaltec-app.ports.dtos.in.customer :as dtos.in.customer]
            [musicaltec-app.ports.dtos.out.customer :as dtos.out.customer]
            [schema.core :as s]))

(def ^:private default-per-page 10)

(s/defn dto->model :- models.pagination/Query
  "ListCustomersIn → Query (model, com defaults resolvidos)."
  [{:keys [q page per-page]} :- dtos.in.customer/ListCustomersIn]
  {:query    (or q "")
   :page     (or page 1)
   :per-page (or per-page default-per-page)})

(s/defn model->dto :- dtos.out.customer/ListCustomersOut
  "PaginatedCustomer (model) → ListCustomersOut, mapeando cada item com `item->dto`."
  [paginated :- models.customer/PaginatedCustomer
   item->dto :- (s/=> dtos.out.customer/CustomerOut models.customer/Customer)]
  {:items       (mapv item->dto (:items paginated))
   :query       (:query paginated)
   :page        (:page paginated)
   :total-pages (:total-pages paginated)
   :total       (:total paginated)
   :per-page    (:per-page paginated)})
