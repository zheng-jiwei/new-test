(ns clojure.handler
    (:require [compojure.core :refer :all]
      [compojure.route :as route]
      [ring.middleware.defaults :refer [wrap-defaults site-defaults]]
      [clojure.data.json :as json]
      [clojure.process :as cp]
      [clojure.java.io :as io]
      [ring.util.response :as response]
      )
    )

(defroutes app-routes
      (GET "/*" request (response/response (io/file (str "resources" (:uri request)))))
      (POST "/calculate" request (cp/process-delivery-price request))
      (route/not-found "Not Found")
   )

(def app
  (-> app-routes
      (wrap-defaults (-> site-defaults
                         (assoc-in [:params :multipart] true)
                         (assoc-in [:security :anti-forgery] false))))
  )
