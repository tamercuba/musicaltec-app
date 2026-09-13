(ns musicaltec-app.adapters.logs.writer
  (:require [clojure.java.io :as io]
            [clojure.pprint :as pp]
            [schema.core :as s]
            [musicaltec-app.ports.logs.core :as ports.logs]))

(defn- timestamp []
  (str (java.time.Instant/now)))

(s/defrecord WriterLogger [writer :- s/Any]
  ports.logs/Logger
  (log! [_ level data]
    (pp/pprint (merge {:level level :timestamp (timestamp)} data) writer)
    (.flush writer)))

(s/defn ->writer-logger :- (s/protocol ports.logs/Logger)
  [target :- s/Any]
  (map->WriterLogger {:writer (io/writer target)}))
