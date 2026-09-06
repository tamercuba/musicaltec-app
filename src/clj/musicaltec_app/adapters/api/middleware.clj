(ns musicaltec-app.adapters.api.middleware
  (:require [ring.middleware.session :as session]
            [ring.middleware.session.memory :as memory]
            [schema.core :as s]))

(def ^:private Handler    (s/=> s/Any s/Any))
(def ^:private Middleware (s/=> Handler Handler))

(def ^:private error-status
  {:auth/unauthorized        401
   :auth/invalid-credentials 401
   :auth/invalid-csrf-token  403
   :customer/tax-id-taken    409
   :customer/email-taken     409
   :resource/not-found       404
   :resource/conflict        409})

(s/defn coerce-dto :- Handler
  [handler :- Handler]
  (fn [request]
    (handler (assoc request :dto (reduce merge (vals (:parameters request)))))))

(s/defn wrap-adapters :- Middleware
  [adapters-map :- s/Any]
  (fn [handler]
    (fn [request]
      (handler (assoc request :adapters adapters-map)))))

(s/defn wrap-errors :- Handler
  [handler :- Handler]
  (fn [request]
    (try
      (handler request)
      (catch clojure.lang.ExceptionInfo e
        (let [{:keys [code]} (ex-data e)]
          (if-let [status (get error-status code)]
            {:status status
             :body   {:code code}}
            (throw e)))))))

(s/defn wrap-session :- Handler
  [handler :- Handler]
  (session/wrap-session handler {:store (memory/memory-store)}))

(s/defn wrap-auth :- Handler
  [handler :- Handler]
  (fn [{:keys [session uri] :as request}]
    (if (or (:user session) (= uri "/api/login"))
      (handler request)
      {:status 401
       :body   {:code :auth/unauthorized}})))

(s/defn wrap-csrf :- Handler
  [handler :- Handler]
  (fn [{:keys [session uri request-method] :as request}]
    (let [unsafe? (contains? #{:post :put :patch :delete} request-method)
          exempt? (= uri "/api/login")
          token   (:csrf-token session)
          valid?  (or (not unsafe?)
                      exempt?
                      (and token (= token (get-in request [:headers "x-csrf-token"]))))]
      (if valid?
        (handler request)
        {:status 403
         :body   {:code :auth/invalid-csrf-token}}))))
