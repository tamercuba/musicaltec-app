(ns musicaltec-app.adapters.api.router
  (:require [muuntaja.middleware :as muuntaja]
            [reitit.coercion.schema :as rcs]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as rrc]
            [ring.middleware.params :as ring-params]
            [musicaltec-app.adapters.api.auth :as api.auth]
            [musicaltec-app.adapters.api.customer :as api.customer]
            [musicaltec-app.adapters.api.financial-movement :as api.financial-movement]
            [musicaltec-app.adapters.api.stock-item :as api.stock-item]
            [musicaltec-app.adapters.api.middleware :as api.middleware]))

(def ^:private coercion
  rcs/coercion)

(defn router
  [adapters
   config]
  (ring/router
   [["/api/login" {:post (api.auth/login-handler (:password config))}]
    ["/api" (into [] cat [api.customer/routes
                          api.stock-item/routes
                          api.financial-movement/routes])]]
   {:data {:coercion coercion
           :middleware [rrc/coerce-exceptions-middleware
                        rrc/coerce-request-middleware
                        api.middleware/coerce-dto
                        (api.middleware/wrap-adapters adapters)]}}))

(defn handler
  [router]
  (-> (ring/ring-handler router (ring/create-default-handler))
      muuntaja/wrap-params
      ring-params/wrap-params
      api.middleware/wrap-errors
      api.middleware/wrap-auth
      api.middleware/wrap-csrf
      muuntaja/wrap-format
      api.middleware/wrap-session))
