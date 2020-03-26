(use 'process.core)
(use 'clojure.test)
(use 'clojure.process)

(def prepare_info (atom {:total_price "0"
                         :total_weight "0"
                         :is_member false
                         :position ""
                         :delivery_price 0
                         :delivery_price2 0
                         }))


(Given #"^\"([^\"]*)\"の場合、購入商品の総額(\d+)円の荷物を\"([^\"]*)\"まで運送する$" [member, price, province]
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
    (swap! prepare_info assoc :delivery_price member_price :delivery_price2 guest_price)
    )
  )

(Then #"^比率は([0-9]+[\.]?[0-9]*)$" [arg1]
  (assert (= (:delivery_price @prepare_info) (int (* (Float/parseFloat arg1) (:delivery_price2 @prepare_info)))))
  )

(Given #"^\"([^\"]*)\"は重さ(\d+)kgの商品を\"([^\"]*)\"まで運送する$" [arg1 arg2 arg3]
  (swap! prepare_info assoc :is_member (= "会員" arg1) :total_weight (* 1000 (str-to-int arg2)) :position arg3)
  )

(When #"^購入商品の総額は(\d+)円$" [arg1]
(swap! prepare_info assoc :total_price arg1)
  )

(Then #"^送料は(\d+)円$" [arg1]
  (assert (= arg1 (str (calculate_delivery_price (:is_member @prepare_info)
                                   (:total_price @prepare_info)
                                   (:total_weight @prepare_info)
                                   (:position @prepare_info)
                                   ))))
  )
