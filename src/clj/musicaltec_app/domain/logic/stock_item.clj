(ns musicaltec-app.domain.logic.stock-item
  (:require [clojure.string :as str]
            [musicaltec-app.domain.logic.pagination :as logic.pagination]
            [musicaltec-app.domain.logic.search :as logic.search]
            [musicaltec-app.domain.models.pagination :as models.pagination]
            [musicaltec-app.domain.models.stock-item :as models.stock-item]
            [schema.core :as s]))

(s/defn ^:private matches? :- s/Bool
  [item :- models.stock-item/StockItem
   q :- s/Str]
  (or (not (seq q))
      (str/includes? (logic.search/normalize (:stock-item/name item)) q)))

(s/defn filter-matching :- [models.stock-item/StockItem]
  [items                     :- [models.stock-item/StockItem]
   {:keys [query available?]} :- models.pagination/Query]
  (->> items
       (filter #(matches? % (logic.search/normalize query)))
       (filter #(or (nil? available?) (= available? (:stock-item/available? %))))
       vec))

(s/defn sort-by-name :- [models.stock-item/StockItem]
  [items :- [models.stock-item/StockItem]]
  (sort-by (juxt :stock-item/name :stock-item/id) items))

(s/defn ->paginated :- models.stock-item/PaginatedStockItem
  [items :- [models.stock-item/StockItem]
   query :- models.pagination/Query]
  (logic.pagination/->paginated items query))
