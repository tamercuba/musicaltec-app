(ns musicaltec-app.components.alert)

(def kind-class
  {:danger  "p-4 mb-4 text-sm text-fg-danger-strong rounded-base bg-danger-soft"
   :brand   "p-4 mb-4 text-sm text-fg-brand-strong rounded-base bg-brand-softer"
   :success "p-4 mb-4 text-sm text-fg-success-strong rounded-base bg-success-soft"
   :warning "p-4 mb-4 text-sm text-fg-warning rounded-base bg-warning-soft"
   :neutral "p-4 mb-4 text-sm text-heading rounded-base bg-neutral-secondary-medium"})

(defn alert
  "Alerta do Flowbite.

  opts — {:kind :danger | :brand | :success | :warning | :neutral (default :danger)
          :body <hiccup>}"
  [{:keys [kind body] :or {kind :danger}}]
  [:div {:role "alert" :class (kind-class kind)} body])
