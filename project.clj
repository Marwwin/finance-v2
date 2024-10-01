(defproject my-app "0.1.0-SNAPSHOT"
  :description "Full-stack Clojure/ClojureScript app with Re-frame and Shadow-cljs"
  :dependencies [[org.clojure/clojure "1.12.0"]
                 [ring "1.9.3"]
                 [compojure "1.6.2"]
                 [ring/ring-defaults "0.3.3"]
                 [org.xerial/sqlite-jdbc "3.42.0.0"]       ;; SQLite JDBC driver
                 [com.github.seancorfield/next.jdbc "1.3.939"]]       ;; JDBC for SQL operations
  :source-paths ["src"]
  :main server.server
  :profiles {:dev {:dependencies [[cider/cider-nrepl "0.26.0"]]
                   :plugins [[cider/cider-nrepl "0.26.0"]]}})
