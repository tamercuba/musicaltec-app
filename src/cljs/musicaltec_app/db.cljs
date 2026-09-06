(ns musicaltec-app.db
  (:require [musicaltec-app.config :as config]))

(def empty-form
  {:name   ""
   :phones [""]
   :email  ""
   :kind   :person
   :tax-id ""})

(def default-db
  {:auth      {:logged-in?   false
               :logging-in?  false
               :login-error  nil
               :csrf-token   nil}
   :route     {:name         nil
               :path-params  {}
               :query-params {}}
   :theme     {:dark?        false}
   :customers {:items        []
               :query        ""
               :page         1
               :per-page     config/default-per-page
               :total        0
               :total-pages  0
               :loading?     false
               :error        nil
               :drawer       {:mode    nil
                              :open?   false
                              :id      nil
                              :saving? false
                              :error   nil
                              :form    empty-form}}})
