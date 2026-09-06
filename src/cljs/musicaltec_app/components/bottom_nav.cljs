(ns musicaltec-app.components.bottom-nav)

(def nav-class
  "fixed bottom-0 left-0 z-40 w-full h-16 bg-neutral-primary-soft border-t border-default")

(def grid-class
  "grid h-full max-w-xl mx-auto font-medium")

(def item-class
  "inline-flex flex-col items-center justify-center px-5 hover:bg-neutral-secondary-medium group")

(def cols->class
  {3 "grid-cols-3"
   4 "grid-cols-4"
   5 "grid-cols-5"
   6 "grid-cols-6"})

(defn item
  "Um item da bottom navigation.

  item — {:label \"Início\" :href \"/\" :icon-path \"...\" :active? bool}"
  [{:keys [label href icon-path active?]}]
  (let [color (if active? "text-fg-brand" "text-body")]
    [:a {:href href :class item-class :aria-current (when active? "page")}
     [:svg {:class (str "w-6 h-6 mb-1 " color " group-hover:text-fg-brand")
            :aria-hidden "true" :xmlns "http://www.w3.org/2000/svg"
            :width "24" :height "24" :fill "none" :viewBox "0 0 24 24"}
      [:path {:stroke "currentColor" :stroke-linecap "round" :stroke-linejoin "round"
              :stroke-width "2" :d icon-path}]]
     [:span {:class (str "text-sm " color " group-hover:text-fg-brand")}
      label]]))

(defn bottom-nav
  "Barra de navegação inferior (bottom navigation).

  opts — {:items [item ...]}"
  [{:keys [items]}]
  [:div {:class nav-class}
   [:div {:class (str grid-class " " (cols->class (count items)))}
    (map item items)]])
