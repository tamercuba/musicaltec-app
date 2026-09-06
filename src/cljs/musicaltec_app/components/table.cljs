(ns musicaltec-app.components.table
  (:require [musicaltec-app.components.util :as u]))

(def table-class
  "w-full text-sm text-left rtl:text-right text-body")

(def thead-class
  "text-xs text-body uppercase bg-neutral-secondary-soft")

(def th-class
  "px-6 py-3 font-medium")

(def tr-class
  "bg-neutral-primary border-b border-default hover:bg-neutral-secondary-strongest")

(def th-row-class
  "px-6 py-4 font-medium text-heading whitespace-nowrap")

(def td-class
  "px-6 py-4")

(defn table [attrs & body]
  [:table (u/merge-attrs {:class table-class} attrs) body])

(defn thead [attrs & body]
  [:thead (u/merge-attrs {:class thead-class} attrs) body])

(defn tbody [attrs & body]
  [:tbody (u/merge-attrs {} attrs) body])

(defn tr [attrs & body]
  [:tr (u/merge-attrs {:class tr-class} attrs) body])

(defn th [attrs & body]
  [:th (u/merge-attrs {:scope "col" :class th-class} attrs) body])

(defn th-row [attrs & body]
  [:th (u/merge-attrs {:scope "row" :class th-row-class} attrs) body])

(defn td [attrs & body]
  [:td (u/merge-attrs {:class td-class} attrs) body])
