(ns de.sveri.custman.routes.customer
  (:require [compojure.core :refer [routes GET POST]]
            [ring.util.response :refer [response redirect]]
            [bouncer.core :as bc]
            [bouncer.validators :as bv]
            [com.rpl.specter :as spect]
            [clojure.string :as str]
            [failjure.core :as f]
            [noir.session :as sess]
            [clojure.instant :as inst]
            [clj-time.format :as time-f]
            [clj-time.coerce :as time-co]
            [de.sveri.custman.layout :as layout]
            [de.sveri.custman.db.customer :as db-cust]
            [de.sveri.custman.locale :as loc]))
            ;[clojure.spec :as s]))

(defn index-page []
  (layout/render "customer/index.html"))

(defn add-page [{:keys [locale]}]
  (layout/render "customer/add.html" {:language locale :date-format (loc/get-datepicker-format locale)}))

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


(defn add [{:keys [params localize locale]} db]
  ;(clojure.pprint/pprint (time-co/to-date (time-f/parse (time-f/formatter date-format) (:birthday params))))
  (f/attempt-all [_ (validate-customer params localize)
                  _ (db-cust/insert-customer db (assoc params
                                                  :birthday (time-co/to-long
                                                              (time-f/parse
                                                                (time-f/formatter (loc/get-date-java-format locale))
                                                                (:birthday params))))
                                             (sess/get :user-id)
                                             localize)]
    (do
      (layout/flash-result (localize [:customer/added]) "alert-success")
      (redirect "/customer"))
    (f/when-failed [e]
      (layout/flash-result (:message e) "alert-danger")
      (layout/render "customer/add.html"))))



(defn customer-routes [db]
  (routes
    (GET "/customer" [] (index-page))
    (GET "/customer/add" req (add-page req))
    (POST "/customer/add" req (add req db))))

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