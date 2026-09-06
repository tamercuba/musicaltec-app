(ns musicaltec-app.ports.dtos.out.error
  (:require [schema.core :as s]))

(s/defschema ErrorCode
  (s/enum :auth/unauthorized
          :auth/invalid-credentials
          :auth/invalid-csrf-token
          :customer/tax-id-taken
          :customer/email-taken
          :resource/not-found
          :resource/conflict))

(s/defschema ErrorOut
  {:code ErrorCode})
