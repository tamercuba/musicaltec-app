(ns musicaltec-app.adapters.db.sql.core
  (:require [clojure.string :as str]
            [migratus.core :as migratus]
            [next.jdbc :as jdbc]
            [next.jdbc.result-set :as rs])
  (:import [java.time Instant]
           [org.sqlite SQLiteDataSource]))

(defn- sqlite-datasource
  [^String url]
  (doto (SQLiteDataSource.)
    (.setUrl url)))

(defn ->datasource
  [^String database-url]
  (cond
    (str/starts-with? database-url "jdbc:sqlite:")     (sqlite-datasource database-url)
    (str/starts-with? database-url "jdbc:postgresql:") (jdbc/get-datasource {:jdbcUrl database-url})
    :else (throw (ex-info "unsupported database-url" {:url database-url}))))

(defn migrate!
  [datasource migration-dir]
  (let [result (migratus/migrate {:store         :database
                                  :db            {:datasource datasource}
                                  :migration-dir migration-dir})]
    (when (not (nil? result))
      (throw (ex-info "schema migration failed" {:result result})))))

(def ^:private builder-fn rs/as-unqualified-kebab-maps)

(defn execute!
  [connectable sql-vec]
  (jdbc/execute! connectable sql-vec {:builder-fn builder-fn}))

(defn execute-one!
  [connectable sql-vec]
  (jdbc/execute-one! connectable sql-vec {:builder-fn builder-fn}))

(defn with-transaction
  [connectable f]
  (jdbc/with-transaction [tx connectable] (f tx)))

(defn query!
  [connectable sql-vec]
  (execute! connectable sql-vec))

(defn query-one!
  [connectable sql-vec]
  (execute-one! connectable sql-vec))

(defn now
  "Timestamp ISO-8601 UTC"
  []
  (.toString (Instant/now)))

(defn update-count
  [result]
  (long (or (-> result first :next.jdbc/update-count) 0)))
