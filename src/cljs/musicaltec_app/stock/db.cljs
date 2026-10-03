(ns musicaltec-app.stock.db
  (:require [musicaltec-app.core.config :as config]))

(defn- today []
  (subs (.toISOString (js/Date.)) 0 10))

(def empty-item-form
  {:name "" :brand "" :quantity "" :cost "" :default-price "" :serial "" :notes ""})

(defn empty-movement-form
  []
  {:stock-item-id      nil
   :name               ""
   :brand              ""
   :quantity           "1"
   :unit-price         ""
   :default-price      ""
   :serial             ""
   :notes              ""
   :date               (today)
   :counterparty       ""
   :parcelado?         false
   :installments-count "2"
   :has-down-payment?  false
   :down-payment       ""})

(def default-db
  {:stock {:tab             :available    ; :available | :sold-out
           :items           []
           :query           ""
           :page            1
           :per-page        config/default-per-page
           :total           0
           :total-pages     0
           :loading?        false
           :error           nil
           :counterparty-results []
           :item-drawer     {:mode    nil     ; :edit
                             :open?   false
                             :id      nil
                             :saving? false
                             :error   nil
                             :form    empty-item-form}
           :movement-drawer {:mode    nil     ; :purchase | :sale
                             :open?   false
                             :saving? false
                             :error   nil
                             :form    (empty-movement-form)}}})
