(ns musicaltec-app.system
  (:require [integrant.core :as ig]
            [ring.adapter.jetty :as jetty]
            [musicaltec-app.adapters.api.router :as api.router]
            [musicaltec-app.adapters.db.core :as db.core]
            [musicaltec-app.adapters.db.customer :as db.customer]
            [schema.core :as s])
  (:gen-class))

(defn- read-config []
  {:port        (Integer/parseInt (or (System/getenv "APP_PORT") "8080"))
   :password    (or (System/getenv "APP_PASSWORD") "CHANGE_ME")
   :datomic-uri (or (System/getenv "DATOMIC_URI") "datomic:mem://musicaltec")})

(defmethod ig/init-key :musicaltec-app/config [_ _]
  (read-config))

(defmethod ig/init-key :musicaltec-app/conn [_ {:keys [config]}]
  (db.core/connect (:datomic-uri config) db.customer/schema))

(defmethod ig/init-key :musicaltec-app/adapters [_ {:keys [conn]}]
  {:db/customer-repo (db.customer/->repository conn)})

(defmethod ig/init-key :musicaltec-app/router [_ {:keys [adapters config]}]
  (api.router/router adapters config))

(defmethod ig/init-key :musicaltec-app/handler [_ {:keys [router]}]
  (api.router/handler router))

(defmethod ig/init-key :musicaltec-app/jetty [_ {:keys [handler config]}]
  (jetty/run-jetty handler {:port (:port config) :join? false}))

(defmethod ig/halt-key! :musicaltec-app/jetty [_ server]
  (.stop server))

(def system-config
  {:musicaltec-app/config   {}
   :musicaltec-app/conn     {:config   (ig/ref :musicaltec-app/config)}
   :musicaltec-app/adapters {:conn     (ig/ref :musicaltec-app/conn)}
   :musicaltec-app/router   {:adapters (ig/ref :musicaltec-app/adapters)
                             :config   (ig/ref :musicaltec-app/config)}
   :musicaltec-app/handler  {:router   (ig/ref :musicaltec-app/router)}
   :musicaltec-app/jetty    {:handler  (ig/ref :musicaltec-app/handler)
                             :config   (ig/ref :musicaltec-app/config)}})

(defonce system (atom nil))

(s/defn -main :- s/Any
  [& args :- [s/Str]]
  (reset! system (ig/init system-config)))
