(ns musicaltec-app.components.pagination
  (:require [musicaltec-app.components.icons :as icons]))

(def nav-class
  "flex flex-col md:flex-row justify-between items-start md:items-center space-y-3 md:space-y-0 p-4")

(def item-class
  "flex items-center justify-center text-sm leading-tight py-2 px-3 text-body bg-neutral-primary-soft border border-default hover:bg-neutral-secondary-medium hover:text-heading")

(def item-current-class
  "flex items-center justify-center text-sm leading-tight py-2 px-3 text-brand border border-default bg-neutral-secondary-soft")

(def arrow-class
  "flex items-center justify-center text-sm py-2 px-3 text-body bg-neutral-primary-soft border border-default hover:bg-neutral-secondary-medium hover:text-heading")

(defn- page-link [on-click label current?]
  [:li
   [:button {:type "button"
             :on-click (when (and on-click (not current?)) #(on-click label))
             :class (if current? item-current-class item-class)
             :aria-current (when current? "page")
             :disabled (when current? true)}
    label]])

(defn- arrow-link [on-click page label icon class]
  [:li
   [:button {:type "button" :on-click #(on-click page) :class class}
    [:span {:class "sr-only"} label]
    icon]])

(defn pagination
  "opts — {:page N :total-pages N :total N :per-page N
           :on-page-change (fn [page])}"
  [{:keys [page total-pages total per-page on-page-change]}]
  (when (pos? total)
    (let [from (inc (* (dec page) per-page))
          to   (min total (* page per-page))]
      [:nav {:class nav-class :aria-label "Paginação"}
       [:span {:class "text-sm font-normal text-body"}
        "Mostrando "
        [:span {:class "font-semibold text-heading"} (str from "-" to)]
        " de "
        [:span {:class "font-semibold text-heading"} (str total)]]
       (when (> total-pages 1)
         [:ul {:class "inline-flex items-stretch -space-x-px"}
          (arrow-link on-page-change (max 1 (dec page)) "Anterior"
                      icons/prev-icon
                      (str arrow-class " rounded-l-base"))
          (for [p (range 1 (inc total-pages))]
            (page-link on-page-change p (= p page)))
          (arrow-link on-page-change (min total-pages (inc page)) "Próxima"
                      icons/next-icon
                      (str arrow-class " rounded-r-base"))])])))
