(ns user
  (:require [integrant.repl :as ig-repl]
            [integrant.repl.state :as ig-state]
            [musicaltec-app.system :as system]
            [fixtures :as fixtures]))

(ig-repl/set-prep! (fn [] system/system-config))

(def go ig-repl/go)
(def halt ig-repl/halt)
(def reset ig-repl/reset)
(def reset-all ig-repl/reset-all)

(defn system []
  ig-state/system)

(defn adapters []
  (:musicaltec-app/adapters (system)))

(defn seed! []
  (fixtures/seed! (adapters)))

(go)

(comment
  (seed!)
  (reset))
