(ns server.server
  (:require [ring.adapter.jetty :refer [run-jetty]]
            [compojure.core :refer [defroutes GET POST]]
            [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
            [next.jdbc :as jdbc]))

(def db {:dbtype "h2" :dbname "finance"})

(def ds (jdbc/get-datasource db))

(defn create-table []
  (jdbc/execute! ds ["
                     create table finance (
                                           id int auto_increment primary key,
                                           bucket
                                           )
                     "]))

(defroutes app-routes
  (GET "/" [] "HEllo from backend"))

(defn start-server []
  ;(create-table)
  (run-jetty (wrap-defaults app-routes site-defaults) {:port 3000 :join? false}))

(defn -main []
  (println "Server started")
  (start-server))
