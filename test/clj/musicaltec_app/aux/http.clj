(ns musicaltec-app.aux.http
  (:require [cheshire.core :as json]
            [ring.mock.request :as mock]
            [state-flow.assertions.matcher-combinators :refer [match?]]
            [state-flow.state :as state]))

(defn- basic-auth [password]
  (str "Basic "
       (.encodeToString (java.util.Base64/getEncoder)
                        (.getBytes (str ":" password) "UTF-8"))))

(defn- body->string [body]
  (cond
    (nil? body) nil
    (string? body) body
    (instance? java.io.InputStream body) (slurp body)
    (bytes? body) (String. ^bytes body)
    :else (str body)))

(defn- parse-json [resp]
  (update resp :body #(some-> % body->string (json/parse-string true))))

(defn- session-id [resp]
  (let [cookie (or (get-in resp [:headers "Set-Cookie"])
                   (get-in resp [:headers "set-cookie"]))]
    (some (fn [c] (when c (second (re-find #"ring-session=([^;\s]+)" c))))
          (cond
            (nil? cookie)        []
            (sequential? cookie) cookie
            :else                [cookie]))))

(defn request
  "Runs an authenticated HTTP request against the context handler and
  returns the response with `:body` parsed as JSON.

  opts — {:body _ (JSON structure)  :auth \"Basic ...\"}"
  [ctx method uri & [opts]]
  (let [{:keys [body auth]} opts]
    (-> (mock/request method uri)
        (mock/header "accept" "application/json")
        (cond-> (:session ctx) (mock/cookie "ring-session" (:session ctx))
                (:csrf ctx)    (mock/header "x-csrf-token" (:csrf ctx))
                auth           (mock/header "authorization" auth)
                body           (mock/json-body body))
        ((:handler ctx))
        parse-json)))

(defn request!
  "State-flow step: runs `request` and stores the response in `key`.

  `uri` may be a string or a fn of ctx (e.g. build the URL from the created id)."
  [key method uri & [opts]]
  (state/modify
   (fn [ctx]
     (assoc ctx key (request ctx method (if (fn? uri) (uri ctx) uri) opts)))))

(defn expect
  "State-flow step: asserts that the response stored in `key` matches `matcher`."
  [matcher key]
  (match? matcher (state/gets key)))

(defn login!
  "State-flow step: logs in (Basic Auth) and stores `:session`, `:csrf` and
  `:login` in the context."
  [password]
  (state/modify
   (fn [ctx]
     (let [resp (request ctx :post "/api/login" {:auth (basic-auth password)})]
       (assoc ctx :session (session-id resp)
              :csrf    (-> resp :body :csrf-token)
              :login   resp)))))
