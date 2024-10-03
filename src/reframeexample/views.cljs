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


(defn- format-bucket-header
  [bucket-name]
  (-> bucket-name
      (string/split #"-")
      (->>
        (map string/capitalize)
        (string/join " "))))


(defn entry
  [id {:keys [name amount edit-name edit-amount to-buffer bucket-name]}]
  [(dynamic-keyword "div.entry.entry-" id) {:key            id
                                            :draggable      true
                                            :style          {:display               "grid"
                                                             :grid-template-columns "1fr auto 80px auto"
                                                             :align-items           "center"
                                                             :gap                   "5px"
                                                             :justify-content       "space-between"
                                                             :background-color      (if to-buffer "orange" "inherit")
                                                             :margin                "0.1em"
                                                             :padding               "0.3em"
                                                             :border-radius         "5px"}
                                            :on-drag-start  (fn [event] (-> event .-dataTransfer (.setData "text" id)))
                                            :on-drop        #(re-frame/dispatch [::events/swap-order bucket-name
                                                                                 id
                                                                                 (-> % .-dataTransfer (.getData "text") int)])
                                            :on-drag-end    (fn [event] (-> event .-dataTransfer (.clearData "text")))
                                            :on-drag-enter  #(.preventDefault %)
                                            :on-drag-over   #(.preventDefault %)}
   ;; [:span.entry-order id]
   ;;    [:span.entry-separator " "]
   (if edit-name
     [:input {:value name
              :on-change #(re-frame/dispatch [::events/update-entry bucket-name id :name (-> % .-target .-value)])
              :on-keyPress #(when (-> % .-code (= "Enter"))
                              (re-frame/dispatch [::events/update-entry bucket-name id :edit-name false]))}]
     [:span.entry-name {:style    {:grid-column "1"}
                        :on-click #(re-frame/dispatch [::events/update-entry bucket-name id :edit-name true])} name])
   [:span.entry-separator " : "]
   (if edit-amount
     [:input {:value       amount
              :style       {:grid-column "3"
                            :text-align  "right"}
              :on-change   #(re-frame/dispatch [::events/update-entry bucket-name (int id) :amount (-> % .-target .-value)])
              :on-keyPress #(when (-> % .-code (= "Enter"))
                              (re-frame/dispatch [::events/update-entry bucket-name id :edit-amount false]))}]
     [:span.entry-amount {:on-click #(re-frame/dispatch [::events/update-entry bucket-name id :edit-amount true])} amount])

   (when (= bucket-name "daily")
     (when to-buffer [:span.buffer "*"]) [:span.entry-buffer {:style {:margin-left "0.5em"
                                                                      :grid-column "4"}}
                                          [(dynamic-keyword "button." bucket-name "-entry-" id "-buffer")
                                           {:on-click #(re-frame/dispatch [::events/update-entry bucket-name id :to-buffer (not to-buffer)])} "buffer"]])
   [:span.entry-delete {:style {:margin-left "0.5em"
                                :grid-column "5"}}
    [(dynamic-keyword "button." bucket-name "-entry-" id "-delete")
     {:on-click #(re-frame/dispatch [::events/delete-entry bucket-name id])} "-"]]])


(defn- add-entry-form
  [bucket-name]
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
  [bucket-name {:keys [entries]}]
  [(dynamic-keyword "div#bucket-" bucket-name) {:style {:margin           "2em"
                                                        :padding          "2em"
                                                        :display          "flex"
                                                        :flex-direction   "column"
                                                        :align-items      "center"
                                                        :background-color "green"}}
   [:h2.bucket-header (format-bucket-header bucket-name)]
   [:h3.bucket-total (str "Total " (total-value-of-bucket entries))]
   [:div.entries
    (map-indexed (fn [i e] (entry i (assoc e :bucket-name bucket-name)))  entries)
    (add-entry-form bucket-name)]])


(defn out-bucket
  [bucket-name {:keys [percent entries]}  total-in]
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
      [:input {:type         "number"
               :defaultValue percent
               :style        {:width "4em"
                              :margin-left "1em"}
               :on-change    #(re-frame/dispatch [::events/set bucket-name "percent" %])}]]
     [:h3 (str "In: " incoming)]
     [:div.entries
      (map-indexed (fn [i a] (entry i (assoc a :bucket-name bucket-name))) entries)
      (add-entry-form bucket-name)]
     [:h4 (str "Netto: " netto)]
     (when (= bucket-name "daily")
       (let [buffer (reduce (fn [acc e] (if (:to-buffer e) (+ acc (int (:amount e))) acc)) 0 entries)]
         (when-not (nil? buffer)
           [:h4 (str "Amount to Buffer: " buffer)])))]))


(defn main-panel
  []
  (let [income            (re-frame/subscribe [::subs/income])
        name              (re-frame/subscribe [::subs/name])
        daily             (re-frame/subscribe [::subs/daily])
        fire-extinguisher (re-frame/subscribe [::subs/fire-extinguisher])
        smile             (re-frame/subscribe [::subs/smile])
        splurge           (re-frame/subscribe [::subs/splurge])]
    [:div
     [:h1 @name]
     (in-bucket "income" @income)
     (let [total (total-value-of-bucket (:entries @income))]
       [:div
        (out-bucket "daily" @daily total)
        (out-bucket "splurge" @splurge total)
        (out-bucket "fire-extinguisher" @fire-extinguisher total)
        (out-bucket "smile" @smile total)])]))
