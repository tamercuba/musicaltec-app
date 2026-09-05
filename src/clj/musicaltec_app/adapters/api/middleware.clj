(ns musicaltec-app.adapters.api.middleware
  (:require [musicaltec-app.domain.errors :as errors]
            [ring.middleware.session :as session]
            [ring.middleware.session.memory :as memory]
            [schema.core :as s]))

(def ^:private Handler    (s/=> s/Any s/Any))
(def ^:private Middleware (s/=> Handler Handler))

(s/defn coerce-dto :- Handler
  "Junta `:parameters` (body+query+path, já coerçados) num único `:dto` no request."
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
        (let [{:keys [type message]} (ex-data e)]
          (cond
            (= type ::errors/not-found)
            {:status 404
             :body {:status 404 :type "not-found" :message "Não encontrado"}}

            (= type ::errors/conflict)
            {:status 409
             :body {:status 409 :type "conflict" :message message}}

            :else (throw e)))))))

(s/defn wrap-session :- Handler
  [handler :- Handler]
  (session/wrap-session handler {:store (memory/memory-store)}))

(s/defn wrap-auth :- Handler
  [handler :- Handler]
  (fn [{:keys [session uri] :as request}]
    (if (or (:user session) (= uri "/api/login"))
      (handler request)
      {:status 401
       :body {:status 401 :type "unauthorized" :message "Não autenticado"}})))

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
         :body {:status 403 :type "csrf" :message "Token CSRF inválido"}}))))
