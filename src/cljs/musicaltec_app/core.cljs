(ns musicaltec-app.core
  (:require [re-frame.core :as rf]
            [reagent.dom :as rdom]
            [reitit.frontend.easy :as rfe]
            [musicaltec-app.db]
            [musicaltec-app.events]
            [musicaltec-app.http]
            [musicaltec-app.router :as router]
            [musicaltec-app.subs]
            [musicaltec-app.views.core :as views]
            [musicaltec-app.views.error-boundary :as error-boundary]))

(defn ^:export init []
  (rf/dispatch-sync [:app/init])
  (rfe/start! router/router
              (fn [match _history]
                (rf/dispatch [:router/navigate match]))
              {:use-fragment false})
  (rdom/render [error-boundary/error-boundary [views/root]] (js/document.getElementById "app")))
