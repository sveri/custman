(ns de.sveri.custman.helper
  (:require [de.sveri.custman.setup :as s]
            [clj-webdriver.taxi :refer :all]))



(defn sign-in [{:keys [name pw link]}]
  (to (str s/test-base-url (or link "admin/users")))
  (quick-fill-submit {"#upper_email" (or name"admin@localhost.de")}
                     {"#upper_password" (or pw "admin")}
                     {"#upper_password" submit}))
