(ns reframeexample.events
  (:require
   [ajax.core :as ajax]
   [cljs.reader :as reader]
   [clojure.string :as s]
   [day8.re-frame.http-fx]
   [re-frame.core :as re-frame]
   [reframeexample.db :as db]))

; (re-frame/reg-event-db
;  ::initialize-db
;  (fn [_ _]
;    (println "init")
;    (let [state (re-frame/dispatch [::get-current-state])]
;      (println state))))

(re-frame/reg-event-fx
 ::initialize-db
 (fn [{:keys [_]}]
   {:db db/default-db
    :http-xhrio {:method          :get
                 :uri             "http://localhost:3000/current-state"
                 :timeout         8000
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::set-state]
                 :on-failure      [::bad-http-result]}}))

(defn walk [m]
  (into {} (map (fn [[k v]]
                  (cond
                    (= (type v) cljs.core/PersistentArrayMap) [(keyword (name k)) (walk v)]
                    (= (type v) cljs.core/PersistentVector) [(keyword (name k)) (mapv walk v)]
                    :else [(keyword (name k)) v]))
                m)))

(re-frame/reg-event-fx
 ::set-state
 (fn [_ [_ [response]]]
   {:db (-> response
            :state
            cljs.reader/read-string
            walk)}))

(re-frame/reg-event-fx
 ::save-db
 (fn [{:keys [db]} new-state]
   {:db db
    :http-xhrio {:method          :post
                 :uri             "http://localhost:3000/save-db"
                 :params          new-state
                 :timeout         8000
                 :format          (ajax/json-request-format)
                 :response-format (ajax/json-response-format {:keywords? true})
                 :on-success      [::good-http-result]
                 :on-failure      [::bad-http-result]}}))

(re-frame/reg-event-fx
 ::good-http-result
 (fn [_ [_ data]]
   (print "good" data)))

(re-frame/reg-event-fx
 ::bad-http-result
 (fn [_ a]
   (print "bad" a)))

(re-frame/reg-event-db
 ::add
 (fn [db [_ bucket event]]
   (js/console.log bucket event)
   (let [e                 (-> event .-target .-parentElement)
         entry-name        (.querySelector e "input.name")
         entry-amount      (.querySelector e "input.amount")
         amount-of-entries (-> (keyword bucket) db :entries count)]
     (update-in
      db
      [(keyword (s/lower-case bucket)) :entries]
      (fn [a]
        (vec (conj a {:name entry-name.value :amount entry-amount.value :order amount-of-entries})))))))

(re-frame/reg-event-db
 ::set
 (fn [db [_ bucket key event]]
   (assoc-in db [(keyword bucket) (keyword key)] (-> event .-target .-value))))

(re-frame/reg-event-db
 ::delete-entry
 (fn [db [_ bucket entry]]
   (update-in db [(keyword bucket) :entries] #(vec (concat (subvec % 0 entry) (subvec % (inc entry)))))))

(re-frame/reg-event-db
 ::swap-order
 (fn [db [_ bucket new-pos orig-pos]]
   (update-in db
              [(keyword bucket) :entries]
              (fn [entries]
                (let [entry-to-move   (nth entries orig-pos)
                      without-element (vec (concat (subvec entries 0 orig-pos) (subvec entries (inc orig-pos))))]
                  (vec (concat (subvec without-element 0 new-pos) [entry-to-move] (subvec without-element new-pos))))))))

(re-frame/reg-event-db
 ::update-entry
 (fn [db [_ bucket entry entry-key entry-value]]
   (assoc-in db [(keyword bucket) :entries entry entry-key] entry-value)))

