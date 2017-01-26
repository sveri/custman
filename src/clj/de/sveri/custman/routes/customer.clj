(ns de.sveri.custman.routes.customer
  (:require [compojure.core :refer [routes GET POST]]
            [de.sveri.custman.layout :as layout]
            [ring.util.response :refer [response redirect]]
            [bouncer.core :as bc]
            [bouncer.validators :as bv]))

(defn index-page []
  (layout/render "customer/index.html"))

(defn add-page []
  (layout/render "customer/add.html"))

{:last-name "",
 :plz "",
 :number "",
 :mail "",
 :city "",
 :birthday "",
 :surname "alpha",
 :state "",
 :street "",
 :handy1 "",
 :landline "",
 :handy2 "",
 :gender "",
 :__anti-forgery-token
            "HM1HiIeVSkHr6QXeizfPON/R3OwXyghoy6JJ0owJ54h+4pBIZ+0/wVxk49a8th8awWrme9hFX5Mnv1Td",
 :country ""}



(defn extract-bouncer-errors [validation-result]
  (when-let [errors (::bc/errors validation-result)]
    errors))



(defn validate-customer [params]
  (->
    (bc/validate params
                :last-name [[bv/required :message "Pflichtfeld: Nachname"]]
                :surname [[bv/required :message "Pflichtfeld: Vorname"]]
                :birthday [[bv/required :message "Pflichtfeld: Geburtstag"]])
    extract-bouncer-errors))



(defn add [params]
  (clojure.pprint/pprint (validate-customer params))
  (redirect "/customer"))


(defn customer-routes []
  (routes
    (GET "/customer" [] (index-page))
    (GET "/customer/add" [] (add-page))
    (POST "/customer/add" req (add (:params req)))))
