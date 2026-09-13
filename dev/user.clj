(ns user
  (:require [clojure.string :as str]
            [datomic.api :as d]
            [integrant.repl :as ig-repl]
            [integrant.repl.state :as ig-state]
            [musicaltec-app.system :as system]
            [fixtures :as fixtures]))

(ig-repl/set-prep! (fn [] system/system-config))

(def go ig-repl/go)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(defn halt
  "Stops the system and deletes the in-memory database."
  []
  (let [uri (get-in ig-state/system [:musicaltec-app/config :datomic-uri])]
    (ig-repl/halt)
    (when (and uri (str/starts-with? uri "datomic:mem://"))
      (try
        (d/delete-database uri)
        (catch Exception _ nil)))))

(defn system []
  ig-state/system)

(defn adapters []
  (:musicaltec-app/adapters (system)))

(defn seed! []
  (fixtures/seed! (adapters)))

(defn seed-many! [n]
  (fixtures/seed-many! n (adapters)))

(comment
  (seed!)
  (seed-many! 100)
  (go)
  (halt)
  (reset)
  (reset-all))

