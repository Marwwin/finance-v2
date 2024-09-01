(ns reframeexample.views
  (:require
   [clojure.string :as string]
   [re-frame.core :as re-frame]
   [reframeexample.events :as events]
   [reframeexample.subs :as subs]))

(defn- total-value-of-bucket
  [entries]
  (reduce
   (fn [acc {:keys [_ amount]}]
     (+ acc (int amount))) 0 entries))

(defn- dynamic-keyword
  [& s]
  (keyword (apply str s)))

(defn- format-bucket-header [bucket-name]
  (-> bucket-name
      (string/split #"-")
      (->>
       (map string/capitalize)
       (string/join " "))))

(defn entry
  [id {:keys [name amount bucket-name]}]
  [(dynamic-keyword "div.entry.entry-" id) {:key            id
                                            :draggable      true
                                            :style {:display         "flex"
                                                    :justify-content "space-between" }
                                            :on-drag-start  (fn [v] (-> v .-dataTransfer (.setData "text" id)))
                                            :on-drop        #(re-frame/dispatch [::events/swap-order bucket-name
                                                                                 id
                                                                                 (-> % .-dataTransfer (.getData "text") int)])
                                            :on-drag-end    (fn [v] (-> v .-dataTransfer (.clearData "text")))
                                            :on-drag-enter  #(.preventDefault %)
                                            :on-drag-over   #(.preventDefault %)}
   [:span.entry-order id]
   [:span.entry-separator " "]
   [:span.entry-name name]
   [:span.entry-separator " : "]
   [:span.entry-amount amount]
   [:span.entry-delete {:style {:margin-left "0.5em"}}
    [(dynamic-keyword "button." bucket-name "-entry-" id)
     {:on-click #(re-frame/dispatch [::events/delete-entry bucket-name (-> %
                                                                           .-target
                                                                           .-parentElement
                                                                           .-parentElement
                                                                           .-className
                                                                           (string/split #"entry-")
                                                                           last
                                                                           int)])}
     "-"]]])

(defn- add-entry-form [bucket-name]
  [:div.addform
   [(dynamic-keyword "span#" bucket-name "-add")
    [:input.name {:type "text"
                  :placeholder "Name"
                  :style {:width "10em"}
                  :on-keyPress #(when (-> % .-code (= "Enter"))
                                  (re-frame/dispatch [::events/add bucket-name %]))}]
    [:input.amount {:type "number"
                    :placeholder "€"
                    :style {:width "4em"}
                    :on-keyPress #(when (-> % .-code (= "Enter"))
                                    (re-frame/dispatch [::events/add bucket-name %]))}]
    [:button {:on-click #(re-frame/dispatch [::events/add bucket-name %])} "+"]]])

(defn in-bucket
  [{:keys [bucket-name entries]}]
  [(dynamic-keyword "div#bucket-" bucket-name) {:style {:margin           "2em"
                                                        :padding          "2em"
                                                        :display          "flex"
                                                        :flex-direction   "column"
                                                        :align-items      "center"
                                                        :background-color "green"}}
   [:h2.bucket-header (format-bucket-header bucket-name)]
   [:h3.bucket-total (str "Total " (total-value-of-bucket entries))]
   (map-indexed (fn [i e] (entry i (assoc e :bucket-name bucket-name)))  entries)
   (add-entry-form bucket-name)])

(defn out-bucket
  [{:keys [bucket-name percent entries]}  total-in]
  (let [incoming  (* percent total-in)
        netto     (- incoming (total-value-of-bucket entries))]
    [(dynamic-keyword "div#bucket-" bucket-name) {:style {:margin           "2em"
                                                          :padding          "2em"
                                                          :display          "flex"
                                                          :flex-direction   "column"
                                                          :align-items      "center"
                                                          :background-color "red"}}
     [:h2 (format-bucket-header bucket-name)]
     [:div {:style {:display "flex"
                    :align-items "center"}}
      [:h3#percentage "Percent: "]
      [:input {:type "number"
               :defaultValue percent
               :style {:width "4em"
                       :margin-left "1em"}
               :on-change #(re-frame/dispatch [::events/set bucket-name "percent" %])}]]
     [:h3 (str "In: " incoming)]
     [:div.entries
      (map-indexed (fn [i a] (entry i (assoc a :bucket-name bucket-name))) entries)
      (add-entry-form bucket-name)]
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
     (in-bucket @income)
     (let [total (total-value-of-bucket (:entries @income))]
       [:div
        (out-bucket @daily total)
        (out-bucket @splurge total)
        (out-bucket @fire-extinguisher total)
        (out-bucket @smile total)])]))




