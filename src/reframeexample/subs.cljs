(ns reframeexample.subs
  (:require
   [re-frame.core :as re-frame]))

(re-frame/reg-sub
 ::income
 (fn [db]
   (:income db)))

(re-frame/reg-sub
 ::name
 (fn [db]
   (:name db)))

(re-frame/reg-sub
 ::daily
 (fn [db]
   (:daily db)))

(re-frame/reg-sub
 ::splurge
 (fn [db]
   (:splurge db)))

(re-frame/reg-sub
 ::fire-extinguisher
 (fn [db]
   (:fire-extinguisher db)))

(re-frame/reg-sub
 ::smile
 (fn [db]
   (:smile db)))
