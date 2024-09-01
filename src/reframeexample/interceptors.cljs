(ns reframeexample.interceptors
  (:require
   [re-frame.core :as re-frame]
   [reframeexample.events :as events]))

(def db-change-interceptor
  (re-frame/->interceptor
   :id :db-change-interceptor
   :after (fn [context]
            (let [old-db        (get-in context [:coeffects :db])
                  new-db        (get-in context [:effects :db])]

              (when (and (not (nil? new-db))
                         (not= old-db new-db)
                         (not (:loading new-db))
                         (not (:loading old-db)))
                (print "change" new-db)
                (re-frame/dispatch [::events/save-db new-db]))
              context))))

(re-frame/reg-global-interceptor db-change-interceptor)
