(ns de.sveri.custman.routes.customer-test
  (:require [clojure.test :refer :all]
            [clj-webdriver.taxi :refer :all]
            [de.sveri.custman.setup :as s]
            [de.sveri.custman.helper :as h]
            [eftest.runner :as eftest]))



(use-fixtures :each s/browser-setup)
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
                     {"#birthday" "1990-03-11"}
                     {"#first-name" submit})
  (is (find-element {:css "div#flash-message.alert-success"})))
  ;(is (.contains (text
  ;                 (find-element {:css "div#flash-message.alert-success"}))
  ;               (s/t [:customer/added]))))
  ;(let [message (text (find-element {:css "div#flash-message"}))]
  ;  (is (.contains message (s/t [:generic/mand-field])))
  ;  (is (.contains message (s/t [:customer/last-name])))
  ;  (is (.contains message (s/t [:customer/birthday])))))