(ns musicaltec-app.components.badge)

(def kind-class
  {:brand   "bg-brand-softer text-fg-brand-strong text-xs font-medium px-1.5 py-0.5 rounded"
   :neutral "bg-neutral-secondary-medium text-heading text-xs font-medium px-1.5 py-0.5 rounded"
   :danger  "bg-danger-soft text-fg-danger-strong text-xs font-medium px-1.5 py-0.5 rounded"
   :success "bg-success-soft text-fg-success-strong text-xs font-medium px-1.5 py-0.5 rounded"
   :warning "bg-warning-soft text-fg-warning text-xs font-medium px-1.5 py-0.5 rounded"})

(defn badge
  "Badge do Flowbite.

  opts — {:kind :brand | :neutral | :danger | :success | :warning (default :neutral)
          :label \"texto\"}"
  [{:keys [kind label] :or {kind :neutral}}]
  [:span {:class (kind-class kind)} label])
