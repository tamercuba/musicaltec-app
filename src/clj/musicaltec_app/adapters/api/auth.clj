(ns musicaltec-app.adapters.api.auth
  (:require [clojure.string :as str]
            [schema.core :as s])
  (:import [java.util Base64]))

(defn- basic-auth-password [request]
  (when-let [auth (get-in request [:headers "authorization"])]
    (let [[scheme encoded] (str/split auth #" " 2)]
      (when (= "Basic" scheme)
        (let [decoded (String. (.decode (Base64/getDecoder) encoded))]
          (when-let [i (str/index-of decoded ":")]
            (subs decoded (inc i))))))))

(s/defn login-handler :- (s/=> s/Any s/Any)
  [password :- s/Str]
  (fn [{:keys [session] :as request}]
    (if (and password (= password (basic-auth-password request)))
      (let [csrf (str (random-uuid))]
        {:status 200
         :session (assoc session :user true :csrf-token csrf)
         :body {:ok true :csrf-token csrf}})
      {:status 401
       :body {:code :auth/invalid-credentials}})))
