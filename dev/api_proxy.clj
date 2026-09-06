(ns api-proxy
  (:require [clojure.string :as str]))

(defn proxy-api?
  [request _config]
  (str/starts-with? (.getRequestTarget request) "/api"))
