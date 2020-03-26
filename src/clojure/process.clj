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

(defn calculate_delivery_price[is_member total_price total_weight position]
  (if (>= (str-to-int (str total_price)) price_limit)
    0
    (let [delivery_price
          (condp = position
                 "北海道"
                 (get_delivery_price_in_hokkaido total_weight)
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

(defn process-delivery-price [request]
  (let [data (:params request)
        isMember (-> request :cookies (get "member") :value)]
    {:status 200
     :body
     (cjson/generate-string {:result (calculate_delivery_price isMember
                                                               (:price data)
                                                               (:weight_select data)
                                                               (codec/url-decode (:province_select data)))
                             })}
    )
  )
