(ns musicaltec-app.logs
  (:require [musicaltec-app.ports.logs.core :as ports.logs]
            [schema.core :as s]))

(s/defrecord ConsoleLogger []
  ports.logs/Logger
  (log! [_ level data]
    (js/console.log (clj->js (merge {:level level :timestamp (.toISOString (js/Date.))} data)))))

(s/defn ->console-logger :- (s/protocol ports.logs/Logger)
  []
  (map->ConsoleLogger nil))
