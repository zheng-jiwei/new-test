(use 'process.core)
(use 'clojure.test)
(use 'clojure.process)
(require 'clojure.process)
(import 'java.util.Date)

(def prepare_info (atom {:total_price "0"
                         :total_weight "0"
                         :is_member false
                         :position ""
                         :delivery_price 0
                         :post_type ""
                         :delivery_time ""
                         :reserve1 0
                         :reserve2 0
                         }))

(defn init_prepare[]
  (reset! prepare_info {:post_type "宅配便" :delivery_time "時間指定なし"})
  )

(Given #"^\"([^\"]*)\"の場合、購入商品の総額(\d+)円の荷物を\"([^\"]*)\"まで運送する$" [member, price, province]
    (init_prepare)
   (swap! prepare_info assoc :total_price price :is_member (= member "会員") :position province)
       )

(When #"^重さ<(\d+)kg$" [weight]
      (swap! prepare_info assoc :delivery_price
             (calculate_delivery_price (:is_member @prepare_info)
                                       (:total_price @prepare_info)
                                       (- (int (* 1000 (str-to-int weight))) 1)
                                       (:position @prepare_info)
                                       )))

(When #"^重さ>=(\d+)kg且つ重さ<(\d+)kg$" [arg1 arg2]
 (swap! prepare_info assoc :delivery_price
        (calculate_delivery_price (:is_member @prepare_info)
                                  (:total_price @prepare_info)
                                  (* 500 (+ (str-to-int arg1) (str-to-int arg2)))
                                  (:position @prepare_info)
                                  ))
 )

(When #"^重さ>=(\d+)kg$" [arg1]
  (swap! prepare_info assoc :delivery_price
        (calculate_delivery_price (:is_member @prepare_info)
                                  (:total_price @prepare_info)
                                  (+ 1 (int (* 1000 (str-to-int arg1))))
                                  (:position @prepare_info)
                                  ))
 )

(When #"^重さ=([0-9]+[\.]?[0-9]*)kg$" [arg1]
   (swap! prepare_info assoc :delivery_price
          (calculate_delivery_price (:is_member @prepare_info)
                                    (:total_price @prepare_info)
                                    (int (* 1000 (Float/parseFloat arg1)))
                                    (:position @prepare_info)
                                    ))
   )

(Then #"^運賃は(\d+)円$" [delivery_price]
    (assert (= delivery_price (str (:delivery_price @prepare_info))))
    )

(Given #"^購入商品の総額(\d+)円、重さ(\d+)kgの荷物を\"([^\"]*)\"まで運送する$" [arg1 arg2 arg3]
  (init_prepare)
  (swap! prepare_info assoc :total_price arg1 :total_weight (* 1000 (str-to-int arg2)) :position arg3)
  )

(When #"^\"([^\"]*)\"ユーザーと\"([^\"]*)\"ユーザーの運賃を比べる$" [arg1 arg2]
  (let [member_price  (calculate_delivery_price (= "会員" arg1)
                                   (:total_price @prepare_info)
                                   (:total_weight @prepare_info)
                                   (:position @prepare_info))
        guest_price (calculate_delivery_price (= "非会員" arg1)
                                         (:total_price @prepare_info)
                                         (:total_weight @prepare_info)
                                         (:position @prepare_info)
                                         )]
    (swap! prepare_info assoc :delivery_price member_price :reserve1 guest_price)
    )
  )

(Then #"^比率は([0-9]+[\.]?[0-9]*)$" [arg1]
  (assert (= (:delivery_price @prepare_info) (int (* (Float/parseFloat arg1) (:reserve1 @prepare_info)))))
  )

(Given #"^\"([^\"]*)\"は重さ(\d+)kgの商品を\"([^\"]*)\"まで運送する$" [arg1 arg2 arg3]
  (init_prepare)
  (swap! prepare_info assoc :is_member (= "会員" arg1) :total_weight (* 1000 (str-to-int arg2)) :position arg3)
  )

(When #"^購入商品の総額は(\d+)円$" [arg1]
  (swap! prepare_info assoc :total_price arg1)
  (swap! prepare_info assoc :delivery_price (calculate_delivery_price (:is_member @prepare_info)
                                   (:total_price @prepare_info)
                                   (:total_weight @prepare_info)
                                   (:position @prepare_info)
                                   (:post_type @prepare_info)
                                   (:delivery_time @prepare_info)
                                   ))
  )

(Given #"^\"([^\"]*)\"は重さ(\d+)kgの(\d+)円商品を\"([^\"]*)\"まで運送する$" [arg1 arg2 arg3 arg4]
  (init_prepare)
  (swap! prepare_info assoc :is_member (= "会員" arg1) :total_weight (* 1000 (str-to-int arg2))
         :total_price arg3 :position arg4)
    )

(When #"^ファンクション\"([^\"]*)\"を呼ぶ$" [arg1]
    (swap! prepare_info assoc :reserve1 arg1)
    (let [time_start (.getTime (Date.))
          function_name (str/split arg1 #"\/")
          _ ((ns-resolve (symbol (get function_name 0)) (symbol (get function_name 1)))
                                  (:is_member @prepare_info)
                                  (:total_price @prepare_info)
                                   (:total_weight @prepare_info)
                                   (:position @prepare_info)
                         )
          time_end (.getTime (Date.))]
          (swap! prepare_info assoc :reserve2 (/ (- time_end time_start) 1000))
      )
  )

(Then #"^所要時間は([>|<|=])([0-9]+[\.]?[0-9]*)秒$" [arg1 arg2]
  (assert ((resolve (symbol arg1)) (:reserve2 @prepare_info) (Float/parseFloat arg2)))
  )

(When #"^\"([^\"]*)\"を利用する$" [arg1]
  (swap! prepare_info assoc :post_type arg1)
  (swap! prepare_info assoc :delivery_price
    (calculate_delivery_price (:is_member @prepare_info)
                                     (:total_price @prepare_info)
                                     (:total_weight @prepare_info)
                                     (:position @prepare_info)
                                      (:post_type @prepare_info)
                                      (:delivery_time @prepare_info)
                                     ))
  )

(When #"^\"([^\"]*)\"で配送時間は\"([^\"]*)\"を指定する$" [arg1 arg2]
  (swap! prepare_info assoc :post_type arg1 :delivery_time arg2)
  (swap! prepare_info assoc :delivery_price
    (calculate_delivery_price (:is_member @prepare_info)
                                     (:total_price @prepare_info)
                                     (:total_weight @prepare_info)
                                     (:position @prepare_info)
                                      (:post_type @prepare_info)
                                      (:delivery_time @prepare_info)
                                     ))
  )

(Then #"^エラーメッセージ\"([^\"]*)\"が表示$" [arg1]
  (assert (= arg1 (-> @prepare_info :delivery_price :error)))
  )
