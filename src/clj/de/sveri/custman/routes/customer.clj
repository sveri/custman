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
            [de.sveri.custman.db.address :as db-addr]
            [de.sveri.custman.locale :as loc]))

(defn index-page [db]
  (layout/render "customer/index.html" {:customers (db-cust/get-customers-by-user db (sess/get :user-id))}))

(defn add-page [{:keys [locale]}]
  (layout/render "customer/add.html" {:language locale :date-format (loc/get-datepicker-format locale)
                                      :form-post-to "/customer/add"}))



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


(defn convert-birthday-for-db [customer locale]
  (assoc customer :birthday (time-co/to-long
                              (time-f/parse
                                (time-f/formatter (loc/get-date-java-format locale))
                                (:birthday customer)))))

(defn add [{:keys [params localize locale]} db]
  (f/attempt-all [_ (validate-customer params localize)
                  customer (db-cust/insert-customer db (convert-birthday-for-db params locale)
                                                       (sess/get :user-id)
                                                       localize)
                  address (db-addr/insert-address db params (first customer) localize)]
                 (do
                   (layout/flash-result (localize [:customer/added]) "alert-success")
                   (redirect "/customer"))
                 (f/when-failed [e]
                                (layout/flash-result (:message e) "alert-danger")
                                (layout/render "customer/add.html" {:language locale :date-format (loc/get-datepicker-format locale)
                                                                    :customer params :address params
                                                                    :form-post-to "/customer/add"}))))


(defn format-birthday-for-edit [customer locale]
  (assoc customer :birthday (time-f/unparse (time-f/formatter (loc/get-date-java-format locale)) (:birthday customer))))

(defn edit-page [customer-id db {:keys [localize locale]}]
  (f/attempt-all [customer (db-cust/get-by-id db (Integer/parseInt customer-id) localize)
                  address (first (db-addr/get-address-by-customer db (Integer/parseInt customer-id) localize))]
                 (layout/render "customer/add.html" {:customer (format-birthday-for-edit customer locale)
                                                     :address address
                                                     :form-post-to "/customer/edit"
                                                     :language locale :date-format (loc/get-datepicker-format locale)})
                 (f/when-failed [e]
                                (layout/flash-result (:message e) "alert-danger")
                                (layout/render "customer/index.html"))))

(defn edit [{:keys [params localize locale]} db]
  (f/attempt-all [_ (validate-customer params localize)
                  _ (db-cust/update-customer db (convert-birthday-for-db params locale)
                                                (sess/get :user-id)
                                                localize)
                  _ (db-addr/update-address db params localize)]
                 (do
                   (layout/flash-result (localize [:generic/successful-save]) "alert-success")
                   (redirect "/customer"))
                 (f/when-failed [e]
                                (layout/flash-result (:message e) "alert-danger")
                                (layout/render "customer/add.html" {:customer params
                                                                    :address params
                                                                    :form-post-to "/customer/edit"}))))

(defn customer-routes [db]
  (routes
    (GET "/customer" [] (index-page db))
    (GET "/customer/add" req (add-page req))
    (GET "/customer/edit/:id" [id :as req] (edit-page id db req))
    (POST "/customer/add" req (add req db))
    (POST "/customer/edit" req (edit req db))))