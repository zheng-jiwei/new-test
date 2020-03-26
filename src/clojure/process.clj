(ns clojure.process
    (:gen-class)
  (:require [ring.util.codec :as codec]
            [cheshire.core :as cjson]
            )
    )


(def price_limit 20000)
(def discount_rate 0.2)

(defn str-to-int [str]
 (int (Float/parseFloat str))
 )

(defn get_delivery_price_in_hokkaido [total_weight]
  (let [weight (str-to-int (str total_weight))]
    (cond
      (< weight 1000)
      500
      (and (< weight 2000) (>= weight 1000))
      600
      (and (< weight 5000) (>= weight 2000))
      800
      (and (< weight 10000) (>= weight 5000))
      1000
      :else 1500
      )
    )
  )

(defn get_delivery_price_in_tohoku [total_weight]
  (let [weight (str-to-int (str total_weight))]
    (cond
      (< weight 1000)
      450
      (and (< weight 2000) (>= weight 1000))
      550
      (and (< weight 5000) (>= weight 2000))
      750
      (and (< weight 10000) (>= weight 5000))
      950
      :else 1200
      )
    )
  )

(defn get_delivery_price_in_tyubu [total_weight]
  (let [weight (str-to-int (str total_weight))]
    (cond
      (< weight 1000)
      400
      (and (< weight 2000) (>= weight 1000))
      500
      (and (< weight 5000) (>= weight 2000))
      700
      (and (< weight 10000) (>= weight 5000))
      900
      :else 1000
      )
    )
  )

(defn get_delivery_price_in_kanto [total_weight]
  (let [weight (str-to-int (str total_weight))]
    (cond
      (< weight 1000)
      300
      (and (< weight 2000) (>= weight 1000))
      400
      (and (< weight 5000) (>= weight 2000))
      500
      (and (< weight 10000) (>= weight 5000))
      600
      :else 900
      )
    )
  )
(defn get_delivery_price_in_kinki [total_weight]
  (let [weight (str-to-int (str total_weight))]
    (cond
      (< weight 1000)
      400
      (and (< weight 2000) (>= weight 1000))
      500
      (and (< weight 5000) (>= weight 2000))
      600
      (and (< weight 10000) (>= weight 5000))
      900
      :else 1200
      )
    )
  )

(defn get_delivery_price_in_tyugoku [total_weight]
  (let [weight (str-to-int (str total_weight))]
    (cond
      (< weight 1000)
      500
      (and (< weight 2000) (>= weight 1000))
      600
      (and (< weight 5000) (>= weight 2000))
      800
      (and (< weight 10000) (>= weight 5000))
      1000
      :else 1500
      )
    )
  )
(defn get_delivery_price_in_shikoku [total_weight]
  (let [weight (str-to-int (str total_weight))]
    (cond
      (< weight 1000)
      550
      (and (< weight 2000) (>= weight 1000))
      650
      (and (< weight 5000) (>= weight 2000))
      850
      (and (< weight 10000) (>= weight 5000))
      1100
      :else 1800
      )
    )
  )
(defn get_delivery_price_in_kyusyu [total_weight]
  (let [weight (str-to-int (str total_weight))]
    (cond
      (< weight 1000)
      600
      (and (< weight 2000) (>= weight 1000))
      700
      (and (< weight 5000) (>= weight 2000))
      900
      (and (< weight 10000) (>= weight 5000))
      1200
      :else 2000
      )
    )
  )

(defn calculate_delivery_price
  ([is_member total_price total_weight position]
    (if (>= (str-to-int (str total_price)) price_limit)
      0
      (let [delivery_price
            (condp = position
              "北海道"
              (get_delivery_price_in_hokkaido total_weight)
              "東北"
              (get_delivery_price_in_tohoku total_weight)
              "中部"
              (get_delivery_price_in_tyubu total_weight)
              "関東"
              (get_delivery_price_in_kanto total_weight)
              "近畿"
              (get_delivery_price_in_kinki total_weight)
              "中国"
              (get_delivery_price_in_tyugoku total_weight)
              "四国"
              (get_delivery_price_in_shikoku total_weight)
              "九州"
              (get_delivery_price_in_kyusyu total_weight)
               "-1"
               )
            ]
        (if is_member
          (int (* delivery_price (- 1 discount_rate)))
          delivery_price
          )
        )
      )
    )
  ([is_member total_price total_weight position post_type]
    (let [result (calculate_delivery_price is_member total_price total_weight position)]
      (if (= post_type "メール便")
        (int (* result 0.4))
        result
        )
      )
    )
  ([is_member total_price total_weight position post_type delivery_time]
    (if (and (= post_type "メール便") (not= delivery_time "時間指定なし"))
      {:error "メール便の時間指定はサポートしない"}
      (let [result (calculate_delivery_price is_member total_price total_weight position post_type)]
        (if (= "時間指定なし" delivery_time)
          result
          (if (= 0 result)
            result
            (+ 50 result)
            )
          )
        )
      )
    )
  )

(defn process-delivery-price [request]
  (let [data (:params request)
        isMember (-> request :cookies (get "member") :value)]
    {:status 200
     :body
     (cjson/generate-string {:result (calculate_delivery_price isMember
                                                               (:price data)
                                                               (:weight_select data)
                                                               (codec/url-decode (:province_select data))
                                                               (codec/url-decode (:post_type data))
                                                               (codec/url-decode (:time_select data))
                                                               )
                             })}
    )
  )
