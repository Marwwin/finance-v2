(ns server.server
  (:gen-class)
  (:require [reitit.ring :as ring]
            [ring.adapter.jetty :as jetty]
            [ring.middleware.cors :refer [wrap-cors]]
            [ring.middleware.json :as middleware]
            [next.jdbc :as jdbc]
            [clojure.data.json :as json]))

(def ds (jdbc/get-datasource {:dbtype "sqlite", :dbname "finance.db"}))

(jdbc/execute!
 ds
 ["CREATE TABLE IF NOT EXISTS finance (id INTEGER PRIMARY KEY AUTOINCREMENT, state TEXT)"])

(def headers
  {"Content-Type"                 "application/json",
   "Access-Control-Allow-Origin"  "*",
   "Access-Control-Allow-Methods" "GET,PUT,POST,DELETE,PATCH,OPTIONS",
   "Access-Control-Allow-Headers" "Authorization, Content-Type"})

(defn get-current-state
  [_]
  (let [state (jdbc/execute!
               ds
               ["SELECT state FROM finance ORDER BY id DESC LIMIT 1"])]
    {:status 200, :headers headers, :body (json/write-str state)}))

(defn save-db-handler
  [request]
  (let [[_ body] (:body request)
        _        (jdbc/execute! ds ["insert into finance(state) values(?)" body])
        fin      (jdbc/execute! ds ["SELECT * FROM finance"])]
    {:status 200, :headers headers, :body {:saved "ok", :id (count fin)}}))

(defn options [_] {:status 200, :headers headers})

(def app
  (-> (ring/ring-handler
       (ring/router [["/current-state" {:get get-current-state}]
                     ["/save-db"       {:post save-db-handler, :options options}]]))
      (middleware/wrap-json-response)))

(defn -main
  "I don't do a whole lot ... yet."
  [& args]
  (jetty/run-jetty (middleware/wrap-json-body app {:keywords? false})
                   {:port 3000, :join? false})
  (println "server running at 3000"))
