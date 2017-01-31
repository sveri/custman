(ns de.sveri.custman.routes.customer-test
  (:require [clojure.test :refer :all]
            [clj-webdriver.taxi :refer :all]
            [de.sveri.custman.setup :as s]
            [de.sveri.custman.helper :as h]
            [eftest.runner :as eftest]
            [de.sveri.custman.db.customer :as db-cust]
            [clj-time.coerce :as time-coe]
            [clj-time.core :as time-c]
            [de.sveri.custman.db.address :as db-addr]))



(use-fixtures :each s/browser-and-db-setup)
(use-fixtures :once s/server-setup)

(deftest ^:selenium save-customer-missing-fields
  (h/sign-in {:link "/customer/add"})
  (quick-fill-submit {"#first-name" submit})
  (let [message (text (find-element {:css "div#flash-message"}))]
    (is (.contains message (s/t [:generic/mand-field])))
    (is (.contains message (s/t [:customer/last-name])))
    (is (.contains message (s/t [:customer/first-name])))
    (is (.contains message (s/t [:customer/birthday]))))
  (quick-fill-submit {"#first-name" "foooo"}
                     {"#first-name" submit})
  (let [message (text (find-element {:css "div#flash-message"}))]
    (is (.contains message (s/t [:generic/mand-field])))
    (is (.contains message (s/t [:customer/last-name])))
    (is (.contains message (s/t [:customer/birthday])))))


(deftest ^:selenium save-customer-with-birthday
  (h/sign-in {:link "/customer/add"})
  (select-option "#gender" {:value "male"})
  (quick-fill-submit {"#first-name" "foooo"}
                     {"#last-name" "bar"}
                     {"#birthday" "03/11/1990"}
                     {"#first-name" submit})
  (is (find-element {:css "div#flash-message.alert-success"}))
  (let [customer (-> (db-cust/get-customers-by-user s/db 1) first)
        birthday (-> customer
                     :birthday
                     .getTime
                     time-coe/from-long)]
    (is (= "foooo" (:first-name customer)))
    (is (= "bar" (:last-name customer)))
    (is (= "male" (:gender customer)))
    (is (= 11 (time-c/day birthday)))
    (is (= 3 (time-c/month birthday)))
    (is (= 1990 (time-c/year birthday)))))

(defn add-customer-with-address []
  (h/sign-in {:link "/customer/add"})
  (select-option "#gender" {:value "male"})
  (quick-fill-submit {"#first-name" "foooo"}
                     {"#last-name" "bar"}
                     {"#birthday" "03/11/1990"}
                     {"#handy1" "1234"}
                     {"#handy2" "2345"}
                     {"#landline" "3456"}
                     {"#email" "mail@foobar"}
                     {"#country" "country"}
                     {"#state" "state"}
                     {"#plz" "plz"}
                     {"#city" "city"}
                     {"#street" "street"}
                     {"#house-number" "number"}
                     {"#house-number" submit}))


(deftest ^:selenium save-customer-with-address
  (add-customer-with-address)
  (is (find-element {:css "div#flash-message.alert-success"}))
  (let [customer (-> (db-cust/get-customers-by-user s/db 1) first)
        address (-> (db-addr/get-address-by-customer s/db (:id customer) s/t) first)]
    (is (= "foooo" (:first-name customer)))
    (is (= "male" (:gender customer)))
    (is (= "1234" (:handy1 address)))
    (is (= "2345" (:handy2 address)))
    (is (= "3456" (:landline address)))
    (is (= "mail@foobar" (:email address)))
    (is (= "country" (:country address)))
    (is (= "city" (:city address)))
    (is (= "state" (:state address)))
    (is (= "plz" (:plz address)))
    (is (= "street" (:street address)))
    (is (= "number" (:house-number address)))))


(deftest ^:selenium list-customers
  (h/sign-in {:link "/customer/add"})
  (add-customer-with-address)
  (h/sign-in {:link "/customer/add"})
  (add-customer-with-address)
  (h/sign-in {:link "/customer"})
  (is (= 2 (count (find-elements {:tag :a :text "bar, foooo"})))))


(deftest ^:selenium edit-customer
  (h/sign-in {:link "/customer/add"})
  (add-customer-with-address)
  (click (first (find-elements {:tag :a :text "bar, foooo"})))
  (is (= 1 (count (find-elements {:tag :input :value "foooo"}))))
  (is (= 1 (count (find-elements {:tag :input :value "03/11/1990"}))))
  (is (= "male" (value (first (selected-options "#gender")))))
  (is (= 1 (count (find-elements {:tag :input :value "number"}))))
  (clear "input#birthday")
  (quick-fill-submit {"#first-name" "f"}
                     {"#last-name" "baz"}
                     {"#birthday" "03/12/1991"}
                     {"#city" "city"}
                     {"#street" "street"}
                     {"#house-number" "new-number"}
                     {"#house-number" submit})
  (let [customer (-> (db-cust/get-customers-by-user s/db 1) first)
        address (-> (db-addr/get-address-by-customer s/db (:id customer) s/t) first)
        birthday (-> customer
                     :birthday
                     .getTime
                     time-coe/from-long)]
    (is (= "foooof" (:first-name customer)))
    (is (= "barbaz" (:last-name customer)))
    (is (= "male" (:gender customer)))
    (is (= "1234" (:handy1 address)))
    (is (= "2345" (:handy2 address)))
    (is (= "3456" (:landline address)))
    (is (= "mail@foobar" (:email address)))
    (is (= "country" (:country address)))
    (is (= "citycity" (:city address)))
    (is (= "state" (:state address)))
    (is (= "plz" (:plz address)))
    (is (= "streetstreet" (:street address)))
    (is (= "numbernew-number" (:house-number address)))
    (is (= 12 (time-c/day birthday)))
    (is (= 3 (time-c/month birthday)))
    (is (= 1991 (time-c/year birthday)))))
