(ns de.sveri.custman.routes.customer-test
  (:require [clojure.test :refer :all]
            [clj-webdriver.taxi :refer :all]
            [de.sveri.custman.setup :as s]
            [de.sveri.custman.helper :as h]
            [eftest.runner :as eftest]
            [de.sveri.custman.db.customer :as db-cust]
            [clj-time.coerce :as time-coe]))



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


(deftest ^:selenium save-customer-missing-fields
  (h/sign-in {:link "/customer/add"})
  (quick-fill-submit {"#first-name" "foooo"}
                     {"#last-name" "bar"}
                     {"#birthday" "03/11/1990"}
                     {"#first-name" submit})
  (is (find-element {:css "div#flash-message.alert-success"}))
  (let [birthday (-> (db-cust/get-customer-by-user s/db 1)
                     first
                     :birthday
                     .getTime
                     time-coe/from-long
                     .getDayOfMonth)]
    (println birthday)))
    ;(is (= 11 (.getDate (:birthday customer))))
    ;(is (= 3 (.getMonth (:birthday customer))))
    ;(is (= 1990 (.getYear (:birthday customer))))))
  ;(is (.contains (text
  ;                 (find-element {:css "div#flash-message.alert-success"}))
  ;               (s/t [:customer/added]))))
  ;(let [message (text (find-element {:css "div#flash-message"}))]
  ;  (is (.contains message (s/t [:generic/mand-field])))
  ;  (is (.contains message (s/t [:customer/last-name])))
  ;  (is (.contains message (s/t [:customer/birthday])))))