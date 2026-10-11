(ns musicaltec-app.ports.db-value
  (:import [java.time Instant]
           [java.util Date UUID]))

(defn uuid->str [x]
  (when x (str x)))

(defn str->uuid [s]
  (when s (UUID/fromString (str s))))

(defn kw->str [x]
  (when x (name x)))

(defn str->kw [s]
  (when s (keyword (str s))))

(defn inst->str [^Date x]
  (when x (.toString (.toInstant x))))

(defn str->inst [s]
  (when s (Date/from (Instant/parse (str s)))))

(defn bool->int [b]
  (if b 1 0))

(defn int->bool [x]
  (= 1 x))
