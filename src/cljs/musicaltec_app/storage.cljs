(ns musicaltec-app.storage
  "Wrappers sobre o localStorage (toleram ambientes onde ele não existe).")

(defn get-item
  [k]
  (try
    (.getItem js/localStorage (name k))
    (catch js/Error _ nil)))

(defn put!
  [k v]
  (try
    (.setItem js/localStorage (name k) (str v))
    (catch js/Error _ nil)))

(defn del!
  [k]
  (try
    (.removeItem js/localStorage (name k))
    (catch js/Error _ nil)))
