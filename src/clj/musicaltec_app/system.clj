(ns musicaltec-app.system
  (:require
   [aero.core :as aero]
   [clojure.java.io :as io]
   [integrant.core :as ig]
   [musicaltec-app.adapters.api.router :as api.router]
   [musicaltec-app.adapters.db.core :as db.core]
   [musicaltec-app.adapters.db.customer :as db.customer]
   [musicaltec-app.adapters.db.registry :as db.registry]
   [musicaltec-app.adapters.logs.writer :as logs]
   [ring.adapter.jetty :as jetty]
   [schema.core :as s])
  (:gen-class))

(def default-config
  {:env         :dev
   :port        8080
   :password    "CHANGE_ME"
   :datomic-uri "datomic:mem://musicaltec"})

(defn- read-config []
  (let [file (io/file "config.edn")]
    (if (.exists file)
      (aero/read-config file)
      default-config)))

(defn- log-target [{:keys [env]}]
  (if (= env :prod)
    (io/file "logs/musicaltec.log")
    System/out))

(defmethod ig/init-key :musicaltec-app/config [_ _]
  (read-config))

(defmethod ig/init-key :musicaltec-app/conn [_ {:keys [config]}]
  (db.core/connect (:datomic-uri config) db.registry/schemas))

(defmethod ig/init-key :musicaltec-app/adapters [_ {:keys [conn config]}]
  {:db/customer-repo (db.customer/->repository conn)
   :log/logger       (logs/->writer-logger (log-target config))})

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
   :musicaltec-app/adapters {:conn     (ig/ref :musicaltec-app/conn)
                             :config   (ig/ref :musicaltec-app/config)}
   :musicaltec-app/router   {:adapters (ig/ref :musicaltec-app/adapters)
                             :config   (ig/ref :musicaltec-app/config)}
   :musicaltec-app/handler  {:router   (ig/ref :musicaltec-app/router)}
   :musicaltec-app/jetty    {:handler  (ig/ref :musicaltec-app/handler)
                             :config   (ig/ref :musicaltec-app/config)}})

(defonce system (atom nil))

(s/defn -main :- s/Any
  [& args :- [s/Str]]
  (reset! system (ig/init system-config)))
