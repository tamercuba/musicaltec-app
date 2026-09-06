(ns musicaltec-app.adapters.api.router
  (:require [muuntaja.middleware :as muuntaja]
            [reitit.coercion.schema :as rcs]
            [reitit.ring :as ring]
            [reitit.ring.coercion :as rrc]
            [ring.middleware.params :as ring-params]
            [musicaltec-app.adapters.api.auth :as api.auth]
            [musicaltec-app.adapters.api.customer :as api.customer]
            [musicaltec-app.adapters.api.middleware :as api.middleware]
            [musicaltec-app.ports.value :as value]
            [schema.core :as s]))

(def ^:private coercion
  (rcs/create
   {:matchers
    {:body     {:default rcs/default-coercion-matcher
                :formats {"application/json" (some-fn value/value-object-matcher
                                                      rcs/json-coercion-matcher)}}
     :string   {:default (some-fn value/value-object-matcher rcs/string-coercion-matcher)}
     :response {:default rcs/default-coercion-matcher}}}))

(s/defn router :- s/Any
  [adapters :- s/Any
   config :- s/Any]
  (ring/router
   [["/api/login" {:post (api.auth/login-handler (:password config))}]
    ["/api" api.customer/routes]]
   {:data {:coercion coercion
           :middleware [rrc/coerce-exceptions-middleware
                        rrc/coerce-request-middleware
                        api.middleware/coerce-dto
                        (api.middleware/wrap-adapters adapters)]}}))

(s/defn handler :- (s/=> s/Any s/Any)
  [router :- s/Any]
  (-> (ring/ring-handler router (ring/create-default-handler))
      muuntaja/wrap-params
      ring-params/wrap-params
      api.middleware/wrap-errors
      api.middleware/wrap-auth
      api.middleware/wrap-csrf
      muuntaja/wrap-format
      api.middleware/wrap-session))
