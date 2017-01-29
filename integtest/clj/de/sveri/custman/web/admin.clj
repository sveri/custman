(ns de.sveri.custman.web.admin
  (:require [clojure.test :refer :all]
            [clj-webdriver.taxi :refer :all]
            [de.sveri.custman.setup :as s]
            [de.sveri.custman.helper :as h]))

(use-fixtures :each s/browser-and-db-setup)
(use-fixtures :once s/server-setup)

(defn ->user [name]
  (quick-fill-submit {"#email" name}
                     {"#password" "bbbbbb"}
                     {"#confirm" "bbbbbb"}
                     {"#email" submit}))

(deftest ^:selenium add-user
  (h/sign-in {})
  (->user "foo@bar.de")
  (is (.contains (text "body") (s/t [:user/user_added])))
  (is (find-element {:css "div#flash-message.alert-success"})))

(deftest ^:selenium add-user-invalid-mail
  (h/sign-in {})
  (quick-fill-submit {"#email" "foo"}
                     {"#password" "bbbbbb"}
                     {"#confirm" "bbbbbb"}
                     {"#email" submit})
  (is (.contains (text "body") (s/t [:user/email_invalid]))))

(deftest ^:selenium pass-min-length
  (h/sign-in {})
  (quick-fill-submit {"#email" "foo"}
                     {"#password" "bb"}
                     {"#confirm" "b"}
                     {"#email" submit})
  (is (.contains (text "body") (s/t [:user/pass_min_length]))))

(deftest ^:selenium pass-dont-match
  (h/sign-in {})
  (quick-fill-submit {"#email" "foo"}
                     {"#password" "bbuaeuiae"}
                     {"#confirm" "bcxvlcvxlc"}
                     {"#email" submit})
  (is (.contains (text "body") (s/t [:user/pass_match]))))

(deftest ^:selenium cancel-delete-user
  (h/sign-in {})
  (let [uname "_foo@bar.de"]
    (->user uname)
    (click (find-element {:css "input.btn.btn-danger"}))
    (click (find-element {:css "input.btn.btn-primary"}))
    (is (.contains (text "body") uname))
    (is (.contains (text "body") (s/t [:generic/deletion_canceled])))))

(deftest ^:selenium delete-user
  (h/sign-in {})
  (let [uname "_aadmin@bar.de"]
    (->user uname)
    (click (find-element {:css "input.btn.btn-danger"}))
    (click (find-element {:css "input.btn.btn-danger"}))
    (is (not (.contains (text "body") uname)))
    (is (.contains (text "body") (s/t [:user/deleted])))))

(deftest ^:selenium set-active->logout->change_password
  (h/sign-in {})
  (let [uname "_foo@bar.de"]
    (->user uname)
    (click (find-element {:tag :input :type "checkbox"}))
    (click (find-element {:tag :input :type "submit" :value (s/t [:admin/update])}))
    (to (str s/test-base-url "user/logout"))
    (h/sign-in {:name uname :pw "bbbbbb" :link "user/changepassword"})
    (quick-fill-submit {"#oldpassword" "bbbbbb"}
                       {"#password" "dddddd"}
                       {"#confirm" "dddddd"}
                       {"#confirm" submit})
    (is (.contains (text "body") (s/t [:user/pass_changed])))
    (h/sign-in {:name uname :pw "dddddd" :link "user/changepassword"})
    (is (.contains (text "body") uname))))
