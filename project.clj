(defproject musicaltec-app "0.1.0"
  :description "Management system for the Musical Tec workshop"
  :dependencies [[org.clojure/clojure "1.12.2"]
                 [thheller/shadow-cljs "3.5.0"]
                 [com.github.seancorfield/next.jdbc "1.3.955"]
                 [com.github.seancorfield/honeysql "2.6.1270"]
                 [migratus/migratus "1.6.8"]
                 [org.xerial/sqlite-jdbc "3.45.1.0"]
                 [org.postgresql/postgresql "42.7.3"]
                 [com.zaxxer/HikariCP "5.1.0"]
                 [metosin/reitit "0.10.1"]
                 [metosin/reitit-schema "0.10.1"]
                 [metosin/muuntaja "0.6.12"]
                 [ring/ring-core "1.15.5"]
                 [ring/ring-jetty-adapter "1.15.5"]
                 [prismatic/schema "1.4.2"]
                 [aero "1.1.6"]
                 [integrant "1.0.1"]
                 [integrant/repl "0.5.1"]
                 [reagent "1.2.0"]
                 [re-frame "1.4.7"]]
  :main ^:skip-aot musicaltec-app.system
  :source-paths ["src/clj" "src/cljc" "src/cljs"]
  :resource-paths ["resources"]
  :test-paths ["test/clj"]
  :test-selectors
  {:unit        (fn [m & _] (.startsWith (str (ns-name (:ns m))) "musicaltec-app.unit."))
   :integration (fn [m & _] (.startsWith (str (ns-name (:ns m))) "musicaltec-app.integration."))}
  :aliases
  {"unit"        ["test" ":unit"]
   "integration" ["test" ":integration"]
   "shadow"      ["run" "-m" "shadow.cljs.devtools.cli"]}
  :profiles
  {:dev {:source-paths ["dev"]
          :dependencies [[nubank/state-flow "5.20.1"]
                         [ring/ring-mock "0.6.2"]]
         :jvm-opts ["-XX:-OmitStackTraceInFastThrow"]}})
