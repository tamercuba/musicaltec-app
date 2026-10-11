(ns musicaltec-app.ports.dtos.db.status
  (:require [schema.core :as s]))

(s/defschema StatusDb (apply s/enum #{:active :inactive}))
