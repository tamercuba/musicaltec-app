(ns user
  (:require [integrant.repl :as ig-repl]
            [integrant.repl.state :as ig-state]
            [musicaltec-app.system :as system]
            [fixtures :as fixtures]))

(ig-repl/set-prep! (fn [] system/system-config))

(def go ig-repl/go)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(defn halt
  "Stops the system."
  []
  (ig-repl/halt))

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

