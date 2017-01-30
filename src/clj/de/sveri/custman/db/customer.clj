(ns de.sveri.custman.db.customer
  (:require [clojure.spec :as s]
            [cuerdas.core :as str]
            [clojure.java.jdbc :as j]
            [failjure.core :as f]
            [clojure.tools.logging :as log])
  (:import (java.sql Timestamp)))

(s/def ::non-empty-string (s/and string? #(not (str/empty? %))))

(s/def ::id number?)
(s/def ::users-id number?)
(s/def ::first-name ::non-empty-string)
(s/def ::last-name ::non-empty-string)
(s/def ::birthday inst?)
(s/def ::gender #{"male" "female" "transgender" "genderneutral"})

(s/def ::customer (s/keys :req-un [::first-name ::last-name ::birthday ::gender]
                          :opt-un [::id]))
(s/def ::customers (s/coll-of ::customer))



(defn enum->pg-enum
  "Convert a keyword value into an enum-compatible object."
  [enum-type enum]
  (doto (org.postgresql.util.PGobject.)
    (.setType enum-type)
    (.setValue enum)))

(def ->gender
  "Convert a unit into a unit_enum enum object"
  (partial enum->pg-enum "gender_enum"))


(s/fdef insert-customer :args (s/cat :db any? :customer ::customer :users-id ::users-id :localize fn?))
(defn insert-customer [db customer user-id localize]
  (try
    (j/insert! db :customer {:users_id user-id :birthday (new Timestamp (:birthday customer))
                             :first_name (:first-name customer)
                             :last_name  (:last-name customer) :gender (-> (:gender customer) ->gender)})
    (catch Exception e (do (log/error e)
                           (f/fail (localize [:generic/error-saving]))))))


(defn get-customers-by-user [db user-id]
  (mapv #(assoc % :gender (-> % :gender str))
        (j/query db ["select * from customer where users_id = ?" user-id]
                 {:identifiers #(.replace % \_ \-)})))

