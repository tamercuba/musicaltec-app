(ns musicaltec-app.adapters.db.sql.errors
  (:require [clojure.string :as str]
            [musicaltec-app.domain.errors :as domain.errors]
            [musicaltec-app.ports.errors.db.core :as db-errors]))

(defn- unique-violation? [^Throwable e]
  (->> (Throwable->map e)
       :via
       (map :message)
       (some #(str/includes? (str/lower-case (str %)) "unique constraint"))))

(defn- unique-violation-column [^Throwable e]
  (->> (Throwable->map e)
       :via
       (map :message)
       (keep (fn [m]
               (or (second (re-find #"UNIQUE constraint failed: [^.]+\.([a-z_]+)" (str m)))
                   (second (re-find #"uq_[a-z]+_([a-z_]+)" (str m))))))
       first))

(defmethod db-errors/fail! :customer
  [^Throwable e _]
  (if (unique-violation? e)
    (domain.errors/fail!
     (or ({"tax_id" :customer/tax-id-taken
           "email"  :customer/email-taken}
          (unique-violation-column e))
         :resource/conflict))
    (throw e)))
