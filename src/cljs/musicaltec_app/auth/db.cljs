(ns musicaltec-app.auth.db)

(def default-db
  {:auth {:logged-in?  false
          :logging-in? false
          :login-error nil
          :csrf-token  nil}})
