(ns de.sveri.custman.routes.customer-test
  (:require [clojure.test :refer :all]
            [clj-webdriver.taxi :refer :all]
            [de.sveri.custman.setup :as s]
            [de.sveri.custman.helper :as h]))



(use-fixtures :each s/browser-setup)
(use-fixtures :once s/server-setup)

(deftest ^:selenium save-food
  (h/sign-in {:link "/customer/add"})
  (quick-fill-submit {"#surname" "alpha"}
                     {"#surname" submit})
  (is (.contains (text "body") "Not Found")))
  ;(clojure.pprint/pprint (:handler (:handler system.repl/system))))
  ;(-> (k/session (:handler (:handler system.repl/system)))
  ;    (k/visit "/customer/add")
  ;    (k/follow-redirect)
  ;    (kt/has (kt/text? "E-Mail") "We are on login page")))
