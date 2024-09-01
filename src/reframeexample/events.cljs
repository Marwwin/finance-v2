(ns reframeexample.events
  (:require
   [re-frame.core :as re-frame]
   [reframeexample.db :as db]
   [day8.re-frame.http-fx]
   [cljs.reader :as reader]
   [ajax.core :as ajax]))

; (re-frame/reg-event-db
;  ::initialize-db
;  (fn [_ _]
;    (println "init")
;    (let [state (re-frame/dispatch [::get-current-state])]
;      (println state))))

(re-frame/reg-event-fx
 ::initialize-db
 (fn [{:keys [db]}]
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
 (fn [db [_ [r]]]
   {:db (-> r
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
   (let [e                 (-> event .-target .-parentElement)
         entry-name        (.querySelector e "input.name")
         entry-amount      (.querySelector e "input.amount")
         amount-of-entries (-> (keyword bucket) db :entries count)]
     (update-in
      db
      [(keyword (clojure.string/lower-case bucket)) :entries]
      (fn [a]
        (conj a {:name entry-name.value :amount entry-amount.value :order amount-of-entries}))))))

(re-frame/reg-event-db
 ::set
 (fn [db [_ bucket k c]]
   (let  [value (-> c .-target .-value)]
     (assoc-in db [(keyword bucket) (keyword k)] value))))

(re-frame/reg-event-db
 ::delete-entry
 (fn [db [_ bucket entry]]
   (update-in db [(keyword bucket) :entries] (fn [es]
                                               (vec (concat (subvec es 0 entry) (subvec es (inc entry))))))))

(re-frame/reg-event-db
 ::swap-order
 (fn [db [_ bucket a b]]
   (update-in db
              [(keyword bucket) :entries]
              (fn [entries]
                (assoc entries a (entries b) b (entries a))))))
