(ns musicaltec-app.router
  (:require [reitit.frontend :as rfr]))

(def routes
  [["/" {:name :home}]
   ["/login" {:name :login}]
   ["/customers" {:name :customers}]])

(def router
  (rfr/router routes))
