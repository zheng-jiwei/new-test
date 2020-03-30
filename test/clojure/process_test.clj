(ns clojure.process-test
    (:require
	  [clojure.string :as str]
	  [clojure.test :refer :all]
    [clojure.process :as cp]
      )
      (:import
        (java.util Date TimeZone Locale Calendar)
      )
    )

(def prepare_info (atom {:total_price "0\n"
                         :total_weight "0\n"
                         :is_member false
                         :position "\n"
                         :post_type "\n"
                         :delivery_time "\n"
                         :reserve1 0
                         :reserve2 0
                         }))

(defn- run_call_function [arg1]
  (cp/calculate_delivery_price (:is_member arg1) (:total_price arg1) (:total_weight arg1)
                               (:position arg1) (:post_type arg1) (:delivery_time arg1))
  )

(defn- run_given_condition_in_scenario
  ([is_member total_price total_weight position post_type delivery_time]
    (swap! prepare_info assoc :is_member is_member :total_price total_price :total_weight total_weight
           :position position :post_type post_type :delivery_time delivery_time)
    )
  ([is_member total_price total_weight position post_type]
      (run_given_condition_in_scenario is_member total_price total_weight position post_type nil)
      )
  ([is_member total_price total_weight position]
    (run_given_condition_in_scenario is_member total_price total_weight position nil)
    )
  )

(defn- run_when_condition_in_scenario_1
  ([arg1 arg2]
     (swap! prepare_info assoc :total_weight (/ (+ arg1 arg2) 2))
     (run_call_function @prepare_info)
     )
  ([arg1]
    (run_when_condition_in_scenario_1 arg1 arg1)
    )
  )

;Scenario-1: "北海道"までの通常運賃
(deftest ^:user001 test-delivery-price-by-Scenario-1
  (testing "Scenario-1 '北海道'までの通常運賃\n"
    (testing "Given '非会員'の場合、購入商品の総額10000円の荷物を'北海道'まで運送する\n"
      (run_given_condition_in_scenario false 10000 nil "北海道")
      (testing " When 重さ<1kg; Then 運賃は500円\n"
        (is (= 500 (run_when_condition_in_scenario_1 999)))
      )
      (testing " When 重さ>=1kg and <=2kg; Then 運賃は600円\n"
        (is (= 600 (run_when_condition_in_scenario_1 1000 2000)))
      )
      (testing " When 重さ>=2kg and <=5kg; Then 運賃は800円\n"
        (is (= 800 (run_when_condition_in_scenario_1 2000 5000)))
      )
      (testing " When 重さ>=5kg and <=10kg; Then 運賃は1000円\n"
        (is (= 1000 (run_when_condition_in_scenario_1 5000 10000)))
      )
      (testing " When 重さ>=10kg; Then 運賃は1500円\n"
        (is (= 1500 (run_when_condition_in_scenario_1 11000)))
      )
      (testing " When 重さ=1kg; Then 運賃は600円(border test)\n"
        (is (= 600 (run_when_condition_in_scenario_1 1000)))
      )
      (testing " When 重さ=2kg; Then 運賃は800円(border test)\n"
        (is (= 800 (run_when_condition_in_scenario_1 2000)))
      )
      (testing " When 重さ=5kg; Then 運賃は1000円(border test)\n"
        (is (= 1000 (run_when_condition_in_scenario_1 5000)))
      )
      (testing " When 重さ=10kg; Then 運賃は1500円(border test)\n"
        (is (= 1500 (run_when_condition_in_scenario_1 10000)))
      )
      (testing " When 重さ=10.1kg; Then 運賃は1500円(border test)\n"
        (is (= 1500 (run_when_condition_in_scenario_1 10001)))
      )
      )
    )
  )

(defn- run_when_condition_in_scenario_2 [arg1 arg2]
     (swap! prepare_info assoc :is_member arg1)
     (swap! prepare_info assoc :reserve1  (run_call_function @prepare_info))
    (swap! prepare_info assoc :is_member arg2)
    (swap! prepare_info assoc :reserve2 (run_call_function @prepare_info))
    (/ (:reserve1 @prepare_info) (:reserve2 @prepare_info))
  )
;Scenario-2: 購入金額<20000円の場合、"会員"なら運賃は20%OFF
(deftest ^:user001 test-delivery-price-by-Scenario-2
  (testing "Scenario-2: 購入金額<20000円の場合、'会員'なら運賃は20%OFF\n"
    (testing " Given 購入商品の総額11000円、重さ10kgの荷物を'北海道'まで運送する\n"
    (run_given_condition_in_scenario false 11000 10000 "北海道")
    (testing "When '会員'ユーザーと'非会員'ユーザーの運賃を比べる; Then 比率は0.8\n"
      (is (= (float 0) (- 0.8 (run_when_condition_in_scenario_2 true false))))
      )
     )
    )
  )

(defn- run_when_condition_in_scenario_3 [arg1]
   (swap! prepare_info assoc :total_price arg1)
   (run_call_function @prepare_info)
  )
;Scenario-3: 20000円以上の商品を購入するなら運賃は無料
(deftest ^:user001 test-delivery-price-by-Scenario-3
  (testing "Scenario-3: 20000円以上の商品を購入するなら運賃は無料\n"
    (testing "'非会員'は重さ10kgの商品を'北海道'まで運送する\n"
      (run_given_condition_in_scenario false nil 10000 "北海道")
      (testing "When 購入商品の総額は20000円; Then 運賃は0円\n"
        (is (= 0 (run_when_condition_in_scenario_3 20000)))
        )
      (testing "When 購入商品の総額は19999円; Then 運賃は1500円\n"
        (is (= 1500 (run_when_condition_in_scenario_3 19999)))
        )
      (testing "When 購入商品の総額は20000円; Then 運賃は0円\n"
        (is (= 0 (run_when_condition_in_scenario_3 20000)))
        )
      )
    )
  (testing "'会員'は重さ10kgの商品を'北海道'まで運送する\n"
    (run_given_condition_in_scenario true nil 10000 "北海道")
    (testing "When 購入商品の総額は20000円; Then 運賃は0円\n"
      (is (= 0 (run_when_condition_in_scenario_3 20000)))
      )
    )
  )

(defn- run_when_condition_in_scenario_4 [arg1]
   (swap! prepare_info assoc :post_type arg1)
   (run_call_function @prepare_info)
  )
;Scenario-4: 宅配便はメール便より高いの確認
(deftest ^:user001 test-delivery-price-by-Scenario-4
  (testing "Scenario-4: 宅配便はメール便より高いの確認\n"
    (testing "'非会員'は重さ10kgの5000円商品を'北海道'まで運送する\n"
       (run_given_condition_in_scenario false 5000 10000 "北海道")
       (testing "'宅配便'は'メール'便より運賃が高い\n"
            (is (> (- (run_when_condition_in_scenario_4 "宅配便") (run_when_condition_in_scenario_4 "メール便")) 0))
            )
       (testing "When '宅配便'を利用する; Then 運賃は1500円\n"
               (is (= 1500 (run_when_condition_in_scenario_4 "宅配便")))
               )
       (testing "When 'メール便'を利用する; Then 運賃は600円\n"
               (is (= 600 (run_when_condition_in_scenario_4 "メール便")))
               )
       )
     )
  )

(defn- run_when_condition_in_scenario_5 [arg1 arg2]
   (swap! prepare_info assoc :post_type arg1 :delivery_time arg2)
   (run_call_function @prepare_info)
  )
;Scenario-5: 配送時間指定されるかされないかの料金が異なるの確認
(deftest ^:user001 test-delivery-price-by-Scenario-5
  (testing "Scenario-5: 配送時間指定されるの確認\n"
    (testing "Given '非会員'は重さ10kgの5000円商品を'北海道'まで運送する\n"
       (run_given_condition_in_scenario false 5000 10000 "北海道")
       (testing "When '宅配便'で配送時間は'12時以前'を指定する; Then 運賃は1550円\n"
               (is (= 1550 (run_when_condition_in_scenario_5 "宅配便" "12時以前")))
               )
       (testing "'宅配便'で配送時間は'時間指定なし'を指定する; Then 運賃は1500円\n"
               (is (= 1500 (run_when_condition_in_scenario_5 "宅配便" "時間指定なし")))
               )
       (testing "'メール便'で配送時間は'12時以前'を指定する; Then エラーメッセージ'メール便の時間指定はサポートしない'が表示\n"
               (is (= "メール便の時間指定はサポートしない" (:error (run_when_condition_in_scenario_5 "メール便" "12時以前"))))
               )
       )
     )
  )

(defn- run_when_condition_in_scenario_6 [arg1 arg2]
  (let [k (keyword arg1)
        ns-sym (symbol (namespace k))
        name-sym (symbol (name k))
        func_name (ns-resolve ns-sym name-sym)
        start_time (.getTime (Date.))
        _ (func_name (:is_member arg2) (:total_price arg2) (:total_weight arg2)
                          (:position arg2) (:post_type arg2) (:delivery_time arg2))
        end_time (.getTime (Date.))
        ]
    (/ (- end_time start_time) 1000)
    )
  )
;  Scenario-6: 運賃検索の所要時間
(deftest ^:user001 test-delivery-price-by-Scenario-6
  (testing "Scenario-6: 運賃検索の所要時間\n"
    (testing "Given '非会員'は重さ10kgの5000円商品を'北海道'まで運送する\n"
       (run_given_condition_in_scenario false 10000 5000 "北海道")
       (testing "When ファンクション'clojure.process/calculate_delivery_price'を呼ぶ; Then 所要時間は<0.1秒\n"
           (is (< (- (run_when_condition_in_scenario_6 "clojure.process/calculate_delivery_price" @prepare_info) 0.1) 0))
           )
       )
     )
  )

;Scenario-7 配送方法は指定しない場合、デフォルトは'宅配便'を利用するの確認
(deftest ^:user001 test-delivery-price-by-Scenario-7
  (testing "Scenario-6:  デフォルト配送方法は宅配便\n"
    (testing "Given '非会員'は重さ10kgの5000円商品を'北海道'まで運送する\n"
       (run_given_condition_in_scenario false 10000 5000 "北海道")
       (testing "When 配送方法は'宅配便'での配送料金が'nil'で配送料金を計算する; Then 配送料金は同じ\n"
           (is (= (run_when_condition_in_scenario_5 "宅配便" nil) (run_when_condition_in_scenario_5 nil nil)))
           )
         (testing "When 配送方法は'メール便'での配送料金が'nil'で配送料金を計算する; Then 配送料金は違う\n"
             (is (not= (run_when_condition_in_scenario_5 "メール便" nil) (run_when_condition_in_scenario_5 nil nil)))
             )
       )
     )
  )

;Scenario-8 配送時間は指定しない場合、デフォルトは'時間指定なし'を利用するの確認
(deftest ^:user001 test-delivery-price-by-Scenario-8
  (testing "Scenario-6: デフォルト配送時間指定は'時間指定なし'\n"
    (testing "Given '非会員'は重さ10kgの5000円商品を'北海道'まで運送する\n"
       (run_given_condition_in_scenario false 10000 5000 "北海道")
       (testing "When '時間指定なし'の指定とnilの指定を計算する; Then 配送料金は同じ\n"
           (is (= (run_when_condition_in_scenario_5 nil "時間指定なし") (run_when_condition_in_scenario_5 nil nil)))
           )
         (testing "When '12時以前'の指定とnilの指定を計算する; Then 配送料金は違う\n"
             (is (not= (run_when_condition_in_scenario_5 nil "12時以前") (run_when_condition_in_scenario_5 nil nil)))
             )
       )
     )
  )

; #########
; #以下は地域毎の料金テスト（北海道と同じテストファンクションを利用している）
; #########
;Scenario-9: "東北"までの通常運賃
(deftest ^:user001 test-delivery-price-by-Scenario-9
  (testing "Scenario-1 '東北'までの通常運賃\n"
    (testing "Given '非会員'の場合、購入商品の総額10000円の荷物を'東北'まで運送する\n"
      (run_given_condition_in_scenario false 10000 nil "東北")
      (testing " When 重さ<1kg; Then 運賃は450円\n"
        (is (= 450 (run_when_condition_in_scenario_1 999)))
      )
      (testing " When 重さ>=1kg and <=2kg; Then 運賃は550円\n"
        (is (= 550 (run_when_condition_in_scenario_1 1000 2000)))
      )
      (testing " When 重さ>=2kg and <=5kg; Then 運賃は750円\n"
        (is (= 750 (run_when_condition_in_scenario_1 2000 5000)))
      )
      (testing " When 重さ>=5kg and <=10kg; Then 運賃は950円\n"
        (is (= 950 (run_when_condition_in_scenario_1 5000 10000)))
      )
      (testing " When 重さ>=10kg; Then 運賃は1200円\n"
        (is (= 1200 (run_when_condition_in_scenario_1 11000)))
      )
      (testing " When 重さ=1kg; Then 運賃は650円(border test)\n"
        (is (= 550 (run_when_condition_in_scenario_1 1000)))
      )
      (testing " When 重さ=2kg; Then 運賃は750円(border test)\n"
        (is (= 750 (run_when_condition_in_scenario_1 2000)))
      )
      (testing " When 重さ=5kg; Then 運賃は950円(border test)\n"
        (is (= 950 (run_when_condition_in_scenario_1 5000)))
      )
      (testing " When 重さ=10kg; Then 運賃は1200円(border test)\n"
        (is (= 1200 (run_when_condition_in_scenario_1 10000)))
      )
      (testing " When 重さ=10.1kg; Then 運賃は1200円(border test)\n"
        (is (= 1200 (run_when_condition_in_scenario_1 10001)))
      )
      )
    )
  )

;Scenario-10: "中部"までの通常運賃
(deftest ^:user001 test-delivery-price-by-Scenario-10
  (testing "Scenario-1 '中部'までの通常運賃\n"
    (testing "Given '非会員'の場合、購入商品の総額10000円の荷物を'中部'まで運送する\n"
      (run_given_condition_in_scenario false 10000 nil "中部")
      (testing " When 重さ<1kg; Then 運賃は400円\n"
        (is (= 400 (run_when_condition_in_scenario_1 999)))
      )
      (testing " When 重さ>=1kg and <=2kg; Then 運賃は500円\n"
        (is (= 500 (run_when_condition_in_scenario_1 1000 2000)))
      )
      (testing " When 重さ>=2kg and <=5kg; Then 運賃は700円\n"
        (is (= 700 (run_when_condition_in_scenario_1 2000 5000)))
      )
      (testing " When 重さ>=5kg and <=10kg; Then 運賃は900円\n"
        (is (= 900 (run_when_condition_in_scenario_1 5000 10000)))
      )
      (testing " When 重さ>=10kg; Then 運賃は1000円\n"
        (is (= 1000 (run_when_condition_in_scenario_1 11000)))
      )
      (testing " When 重さ=1kg; Then 運賃は500円(border test)\n"
        (is (= 500 (run_when_condition_in_scenario_1 1000)))
      )
      (testing " When 重さ=2kg; Then 運賃は700円(border test)\n"
        (is (= 700 (run_when_condition_in_scenario_1 2000)))
      )
      (testing " When 重さ=5kg; Then 運賃は900円(border test)\n"
        (is (= 900 (run_when_condition_in_scenario_1 5000)))
      )
      (testing " When 重さ=10kg; Then 運賃は1000円(border test)\n"
        (is (= 1000 (run_when_condition_in_scenario_1 10000)))
      )
      (testing " When 重さ=10.1kg; Then 運賃は1000円(border test)\n"
        (is (= 1000 (run_when_condition_in_scenario_1 10001)))
      )
      )
    )
  )


;Scenario-11: "関東"までの通常運賃
(deftest ^:user001 test-delivery-price-by-Scenario-11
  (testing "Scenario-1 '関東'までの通常運賃\n"
    (testing "Given '非会員'の場合、購入商品の総額10000円の荷物を'関東'まで運送する\n"
      (run_given_condition_in_scenario false 10000 nil "関東")
      (testing " When 重さ<1kg; Then 運賃は300円\n"
        (is (= 300 (run_when_condition_in_scenario_1 999)))
      )
      (testing " When 重さ>=1kg and <=2kg; Then 運賃は400円\n"
        (is (= 400 (run_when_condition_in_scenario_1 1000 2000)))
      )
      (testing " When 重さ>=2kg and <=5kg; Then 運賃は500円\n"
        (is (= 500 (run_when_condition_in_scenario_1 2000 5000)))
      )
      (testing " When 重さ>=5kg and <=10kg; Then 運賃は600円\n"
        (is (= 600 (run_when_condition_in_scenario_1 5000 10000)))
      )
      (testing " When 重さ>=10kg; Then 運賃は900円\n"
        (is (= 900 (run_when_condition_in_scenario_1 11000)))
      )
      (testing " When 重さ=1kg; Then 運賃は400円(border test)\n"
        (is (= 400 (run_when_condition_in_scenario_1 1000)))
      )
      (testing " When 重さ=2kg; Then 運賃は500円(border test)\n"
        (is (= 500 (run_when_condition_in_scenario_1 2000)))
      )
      (testing " When 重さ=5kg; Then 運賃は600円(border test)\n"
        (is (= 600 (run_when_condition_in_scenario_1 5000)))
      )
      (testing " When 重さ=10kg; Then 運賃は900円(border test)\n"
        (is (= 900 (run_when_condition_in_scenario_1 10000)))
      )
      (testing " When 重さ=10.1kg; Then 運賃は900円(border test)\n"
        (is (= 900 (run_when_condition_in_scenario_1 10001)))
      )
      )
    )
  )

;Scenario-12: "近畿"までの通常運賃
(deftest ^:user001 test-delivery-price-by-Scenario-12
  (testing "Scenario-1 '近畿'までの通常運賃\n"
    (testing "Given '非会員'の場合、購入商品の総額10000円の荷物を'近畿'まで運送する\n"
      (run_given_condition_in_scenario false 10000 nil "近畿")
      (testing " When 重さ<1kg; Then 運賃は400円\n"
        (is (= 400 (run_when_condition_in_scenario_1 999)))
      )
      (testing " When 重さ>=1kg and <=2kg; Then 運賃は500円\n"
        (is (= 500 (run_when_condition_in_scenario_1 1000 2000)))
      )
      (testing " When 重さ>=2kg and <=5kg; Then 運賃は600円\n"
        (is (= 600 (run_when_condition_in_scenario_1 2000 5000)))
      )
      (testing " When 重さ>=5kg and <=10kg; Then 運賃は900円\n"
        (is (= 900 (run_when_condition_in_scenario_1 5000 10000)))
      )
      (testing " When 重さ>=10kg; Then 運賃は1200円\n"
        (is (= 1200 (run_when_condition_in_scenario_1 11000)))
      )
      (testing " When 重さ=1kg; Then 運賃は500円(border test)\n"
        (is (= 500 (run_when_condition_in_scenario_1 1000)))
      )
      (testing " When 重さ=2kg; Then 運賃は600円(border test)\n"
        (is (= 600 (run_when_condition_in_scenario_1 2000)))
      )
      (testing " When 重さ=5kg; Then 運賃は900円(border test)\n"
        (is (= 900 (run_when_condition_in_scenario_1 5000)))
      )
      (testing " When 重さ=10kg; Then 運賃は1200円(border test)\n"
        (is (= 1200 (run_when_condition_in_scenario_1 10000)))
      )
      (testing " When 重さ=10.1kg; Then 運賃は1200円(border test)\n"
        (is (= 1200 (run_when_condition_in_scenario_1 10001)))
      )
      )
    )
  )

;Scenario-13: "中国"までの通常運賃
(deftest ^:user001 test-delivery-price-by-Scenario-13
  (testing "Scenario-1 '中国'までの通常運賃\n"
    (testing "Given '非会員'の場合、購入商品の総額10000円の荷物を'中国'まで運送する\n"
      (run_given_condition_in_scenario false 10000 nil "中国")
      (testing " When 重さ<1kg; Then 運賃は500円\n"
        (is (= 500 (run_when_condition_in_scenario_1 999)))
      )
      (testing " When 重さ>=1kg and <=2kg; Then 運賃は600円\n"
        (is (= 600 (run_when_condition_in_scenario_1 1000 2000)))
      )
      (testing " When 重さ>=2kg and <=5kg; Then 運賃は800円\n"
        (is (= 800 (run_when_condition_in_scenario_1 2000 5000)))
      )
      (testing " When 重さ>=5kg and <=10kg; Then 運賃は1000円\n"
        (is (= 1000 (run_when_condition_in_scenario_1 5000 10000)))
      )
      (testing " When 重さ>=10kg; Then 運賃は1500円\n"
        (is (= 1500 (run_when_condition_in_scenario_1 11000)))
      )
      (testing " When 重さ=1kg; Then 運賃は600円(border test)\n"
        (is (= 600 (run_when_condition_in_scenario_1 1000)))
      )
      (testing " When 重さ=2kg; Then 運賃は800円(border test)\n"
        (is (= 800 (run_when_condition_in_scenario_1 2000)))
      )
      (testing " When 重さ=5kg; Then 運賃は1000円(border test)\n"
        (is (= 1000 (run_when_condition_in_scenario_1 5000)))
      )
      (testing " When 重さ=10kg; Then 運賃は1500円(border test)\n"
        (is (= 1500 (run_when_condition_in_scenario_1 10000)))
      )
      (testing " When 重さ=10.1kg; Then 運賃は1500円(border test)\n"
        (is (= 1500 (run_when_condition_in_scenario_1 10001)))
      )
      )
    )
  )

;Scenario-14: "四国"までの通常運賃
(deftest ^:user001 test-delivery-price-by-Scenario-14
  (testing "Scenario-1 '四国'までの通常運賃\n"
    (testing "Given '非会員'の場合、購入商品の総額10000円の荷物を'四国'まで運送する\n"
      (run_given_condition_in_scenario false 10000 nil "四国")
      (testing " When 重さ<1kg; Then 運賃は550円\n"
        (is (= 550 (run_when_condition_in_scenario_1 999)))
      )
      (testing " When 重さ>=1kg and <=2kg; Then 運賃は650円\n"
        (is (= 650 (run_when_condition_in_scenario_1 1000 2000)))
      )
      (testing " When 重さ>=2kg and <=5kg; Then 運賃は850円\n"
        (is (= 850 (run_when_condition_in_scenario_1 2000 5000)))
      )
      (testing " When 重さ>=5kg and <=10kg; Then 運賃は1100円\n"
        (is (= 1100 (run_when_condition_in_scenario_1 5000 10000)))
      )
      (testing " When 重さ>=10kg; Then 運賃は1800円\n"
        (is (= 1800 (run_when_condition_in_scenario_1 11000)))
      )
      (testing " When 重さ=1kg; Then 運賃は650円(border test)\n"
        (is (= 650 (run_when_condition_in_scenario_1 1000)))
      )
      (testing " When 重さ=2kg; Then 運賃は850円(border test)\n"
        (is (= 850 (run_when_condition_in_scenario_1 2000)))
      )
      (testing " When 重さ=5kg; Then 運賃は1100円(border test)\n"
        (is (= 1100 (run_when_condition_in_scenario_1 5000)))
      )
      (testing " When 重さ=10kg; Then 運賃は1800円(border test)\n"
        (is (= 1800 (run_when_condition_in_scenario_1 10000)))
      )
      (testing " When 重さ=10.1kg; Then 運賃は1800円(border test)\n"
        (is (= 1800 (run_when_condition_in_scenario_1 10001)))
      )
      )
    )
  )

;Scenario-15: "九州"までの通常運賃
(deftest ^:user001 test-delivery-price-by-Scenario-15
  (testing "Scenario-1 '九州'までの通常運賃\n"
    (testing "Given '非会員'の場合、購入商品の総額10000円の荷物を'九州'まで運送する\n"
      (run_given_condition_in_scenario false 10000 nil "九州")
      (testing " When 重さ<1kg; Then 運賃は600円\n"
        (is (= 600 (run_when_condition_in_scenario_1 999)))
      )
      (testing " When 重さ>=1kg and <=2kg; Then 運賃は700円\n"
        (is (= 700 (run_when_condition_in_scenario_1 1000 2000)))
      )
      (testing " When 重さ>=2kg and <=5kg; Then 運賃は900円\n"
        (is (= 900 (run_when_condition_in_scenario_1 2000 5000)))
      )
      (testing " When 重さ>=5kg and <=10kg; Then 運賃は1200円\n"
        (is (= 1200 (run_when_condition_in_scenario_1 5000 10000)))
      )
      (testing " When 重さ>=10kg; Then 運賃は2000円\n"
        (is (= 2000 (run_when_condition_in_scenario_1 11000)))
      )
      (testing " When 重さ=1kg; Then 運賃は900円(border test)\n"
        (is (= 700 (run_when_condition_in_scenario_1 1000)))
      )
      (testing " When 重さ=2kg; Then 運賃は900円(border test)\n"
        (is (= 900 (run_when_condition_in_scenario_1 2000)))
      )
      (testing " When 重さ=5kg; Then 運賃は1200円(border test)\n"
        (is (= 1200 (run_when_condition_in_scenario_1 5000)))
      )
      (testing " When 重さ=10kg; Then 運賃は2000円(border test)\n"
        (is (= 2000 (run_when_condition_in_scenario_1 10000)))
      )
      (testing " When 重さ=10.1kg; Then 運賃は2000円(border test)\n"
        (is (= 2000 (run_when_condition_in_scenario_1 10001)))
      )
      )
    )
  )
