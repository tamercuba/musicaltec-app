(defproject musicaltec-app "0.1.0"
  :description "Sistema de gestão da oficina Musical Tec"
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [thheller/shadow-cljs "3.5.0"]
                 [metosin/reitit "0.10.1"]
                 [metosin/reitit-schema "0.10.1"]
                 [metosin/muuntaja "0.6.12"]
                 [ring/ring-core "1.15.5"]
                 [ring/ring-jetty-adapter "1.15.5"]
                 [ring/ring-session "1.13.0"]
                 [ring/ring-anti-forgery "1.4.0"]
                 [ring/ring-defaults "0.7.1"]
                 [plumatic/schema "1.4.1"]
                 [aero "1.1.6"]
                 [integrant "1.0.1"]
                 [integrant/repl "0.5.1"]
                 [reagent "1.2.0"]
                 [re-frame "1.4.7"]]
  :source-paths ["src/clj" "src/cljc"]
  :resource-paths ["resources"]
  :test-paths ["test/clj"]
  :profiles
  {:dev {:source-paths ["dev"]
         :dependencies [[com.datomic/local "1.0.291"]
                        [nubank/state-flow "5.20.1"]]
         :jvm-opts ["-XX:-OmitStackTraceInFastThrow"]}
   :prod {:dependencies [[com.datomic/peer "1.0.7387"]]}})
