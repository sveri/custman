(ns de.sveri.custman.routes.customer
  (:require [compojure.core :refer [routes GET]]
            [de.sveri.custman.layout :as layout]
            [ring.util.response :refer [response]]))

(defn index-page []
  (layout/render "customer/index.html"))

(defn add-page []
  (layout/render "customer/add.html"))

(defn customer-routes []
  (routes
    (GET "/customer" [] (index-page))
    (GET "/customer/add" [] (add-page))))
