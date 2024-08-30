(ns reframeexample.events
  (:require
   [re-frame.core :as re-frame]
   [reframeexample.db :as db]))

(re-frame/reg-event-db
 ::initialize-db
 (fn [_ _]
   db/default-db))

(re-frame/reg-event-db
 ::add
 (fn [db [_ val]]
   (let [e (.querySelector js/document (str "span#" val "-add"))
         name (.querySelector e "input.name")
         amount (.querySelector e "input.amount")]
     (update-in
      db
      [(keyword (clojure.string/lower-case val)) :entries]
      (fn [a] 
        (conj a {:name name.value :amount amount.value}))))))
