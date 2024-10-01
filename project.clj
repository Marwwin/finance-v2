(defproject my-app "0.1.0-SNAPSHOT"
  :description "Full-stack Clojure/ClojureScript app with Re-frame and Shadow-cljs"
  :dependencies [[org.clojure/clojure     "1.11.1"]
                 [metosin/reitit          "0.7.0"]
                 [com.github.seancorfield/next.jdbc "1.3.939"]
                 [org.xerial/sqlite-jdbc "3.43.0.0"]
                 [org.clojure/data.json   "2.4.0"]
                 [ring/ring-core          "1.10.0"]
                 [ring/ring-jetty-adapter "1.10.0"]
                 [ring-cors               "0.1.13"]
                 [ring/ring-json          "0.5.1"]]
  :source-paths ["src"]
  :main server.server
  :profiles {:dev {:dependencies [[cider/cider-nrepl "0.26.0"]]
                   :plugins [[cider/cider-nrepl "0.26.0"]]}})
