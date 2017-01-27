(ns de.sveri.custman.routes.customer
  (:require [compojure.core :refer [routes GET POST]]
            [de.sveri.custman.layout :as layout]
            [ring.util.response :refer [response redirect]]
            [bouncer.core :as bc]
            [bouncer.validators :as bv]
            [com.rpl.specter :as spect]
            [clojure.string :as str]
            [failjure.core :as f]
            [noir.session :as sess]
            [de.sveri.custman.db.customer :as db-cust]))
            ;[clojure.spec :as s]))

(defn index-page []
  (layout/render "customer/index.html"))

(defn add-page []
  (layout/render "customer/add.html"))

;{:last-name "",
; :plz "",
; :number "",
; :mail "",
; :city "",
; :birthday "",
; :first-name "alpha",
; :state "",
; :street "",
; :handy1 "",
; :landline "",
; :handy2 "",
; :gender "",
; :__anti-forgery-token
;            "HM1HiIeVSkHr6QXeizfPON/R3OwXyghoy6JJ0owJ54h+4pBIZ+0/wVxk49a8th8awWrme9hFX5Mnv1Td",
; :country ""}



(defn extract-bouncer-errors [validation-result]
  (let [errors (first (spect/select [spect/ALL :bouncer.core/errors #(not (nil? %))] validation-result))]
    (when errors
      (f/fail
        (reduce (fn [acc error]
                  (str acc (first error) "<br>"))
                ""
                (vals errors))))))

  ;(clojure.pprint/pprint validation-result)
  ;(when-let [errors (::bc/errors validation-result)]
  ;  (vals errors)))


(defn with-mand-field [to-translate localize]
  (str (localize [:generic/mand-field]) ": " (localize [to-translate])))

(defn validate-customer [params localize]
  (let [mand-field (localize [:generic/mand-field])]
    (->
     (bc/validate params
                  :last-name [[bv/required :message (with-mand-field :customer/last-name localize)]]
                  :first-name [[bv/required :message (with-mand-field :customer/first-name localize)]]
                  :birthday [[bv/required :message (with-mand-field :customer/birthday localize)]])
     extract-bouncer-errors)))


(defn add [params localize db]
  (f/attempt-all [_ (validate-customer params localize)
                  _ (db-cust/insert-customer db params (sess/get :user-id) localize)]
    (do
      (layout/flash-result (localize [:customer/added]) "alert-success")
      (redirect "/customer"))
    (f/when-failed [e]
      (layout/flash-result (:message e) "alert-danger")
      (layout/render "customer/add.html"))))



(defn customer-routes [db]
  (routes
    (GET "/customer" [] (index-page))
    (GET "/customer/add" [] (add-page))
    (POST "/customer/add" req (add (:params req) (:localize req) db))))

(def ft [{:last-name '("Pflichtfeld: Nachname"),
          :birthday '("Pflichtfeld: Geburtstag")}
         {:last-name "",
          :plz "",
          :number "",
          :mail "",
          :city "",
          :birthday "",
          :first-name "alpha",
          :state "",
          :bouncer.core/errors
                     {:last-name '("Pflichtfeld: Nachname"),
                      :birthday '("Pflichtfeld: Geburtstag")},
          :street "",
          :handy1 "",
          :landline "",
          :handy2 "",
          :gender "",
          :__anti-forgery-token
                     "WyDRvN3RBbl0mSHEWZeXCcD8kjzCU0unD3M9VT2m6FuEj5s4U02iMN+DRK/Q7sNQuKPgEeaCcf4Nu14+",
          :country ""}])