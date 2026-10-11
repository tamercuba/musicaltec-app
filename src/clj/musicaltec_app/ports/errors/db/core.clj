(ns musicaltec-app.ports.errors.db.core)

(defmulti fail!
  (fn [_ context] context))

(defmethod fail! :default
  [^Throwable e _]
  (throw e))
