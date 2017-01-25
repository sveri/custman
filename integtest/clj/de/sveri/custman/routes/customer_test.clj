(ns de.sveri.custman.routes.customer-test
  (:require [clojure.test :refer :all]
            [kerodon.core :as k]
            [kerodon.test :as kt]
            [de.sveri.custman.setup :as s]
            [de.sveri.custman.components.handler :as h]))



(use-fixtures :each s/clean-db)
(use-fixtures :once s/server-setup)

(deftest ^:integration retrieve-all-foods
  (clojure.pprint/pprint (:handler (:handler system.repl/system)))
  (-> (k/session (:handler (:handler system.repl/system)))
      (k/visit "/customer/add")
      (kt/has (kt/text? "E-Mail") "We are on login page")))
