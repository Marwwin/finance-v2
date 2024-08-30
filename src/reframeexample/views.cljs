(ns reframeexample.views
  (:require
   [re-frame.core :as re-frame]
   [reframeexample.subs :as subs]
   [reframeexample.events :as events]))


(defn total-value-of-bucket
  [entries]
  (reduce
   (fn [acc {:keys [_ amount]}]
     (+ acc amount)) 0 entries))

(defn dynamic-keyword
  [& s]
  (keyword (apply str s)))

(defn entry
  [id {:keys [name amount]}]
  [(dynamic-keyword "div.entry-" id) {:key (str "entry-" id)}
   [:span.name name]
   [:span.separator " : "]
   [:span.amount amount]])

(defn out-bucket
  [{:keys [bucket-name in entries]}]
  (let [netto (- in (total-value-of-bucket entries))]
    [(dynamic-keyword "div#" bucket-name)
     [:h2 bucket-name]
     [:h3 (str "In: " in)]
     (map-indexed entry entries)
     [:div.addform
      [(dynamic-keyword "span#" bucket-name "-add")
       [:input.name {:type "text"}]
       [:input.amount {:type "number"}]
       [:button
        {:on-click #(re-frame/dispatch [::events/add bucket-name])}]]]
     [:h4 (str "Netto: " netto)]]))

(defn main-panel []
  (let [income            (re-frame/subscribe [::subs/income])
        name              (re-frame/subscribe [::subs/name])
        daily             (re-frame/subscribe [::subs/daily])
        fire-extinguisher (re-frame/subscribe [::subs/fire-extinguisher])
        smile             (re-frame/subscribe [::subs/smile])
        splurge           (re-frame/subscribe [::subs/splurge])]

    [:div
     [:h1 @name]
     [:div#income
      [:h2 "Income"]
      (map-indexed entry (:entries @income))]
     (out-bucket @daily)
     (out-bucket @splurge)
     (out-bucket @fire-extinguisher)
     (out-bucket @smile)]))




