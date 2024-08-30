(ns reframeexample.db)

(def default-db
  {:income            {:bucket-name "income"
                       :entries [{:name "Lön"
                                  :amount 42}]}
   :daily             {:bucket-name "daily"
                       :in 420
                       :entries [{:name "Hyra"
                                  :amount 1250}]}
   :fire-extinguisher {:bucket-name "fire-Extinguisher"
                       :in 420
                       :entries [{:name "Fire"
                                  :amount 1250}]}
   :smile             {:bucket-name "smile"
                       :in 420
                       :entries [{:name "Smile"
                                  :amount 1250}]}
   :splurge           {:bucket-name "splurge"
                       :in 420
                       :entries [{:name "Jonna"
                                  :amount 200}
                                 {:name "Markus"
                                  :amount 200}]}

   :name "Finance"})
