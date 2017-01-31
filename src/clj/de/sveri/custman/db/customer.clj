(ns de.sveri.custman.db.customer
  (:require [clojure.spec :as s]
            [cuerdas.core :as str]
            [clojure.java.jdbc :as j]
            [failjure.core :as f]
            [clj-time.coerce :as time-coe]
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
                           (log/error (.getNextException e))
                           (f/fail (localize [:generic/error-saving]))))))


(defn get-customers-by-user [db user-id]
  (j/query db ["select * from customer where users_id = ?" user-id]
           {:identifiers #(.replace % \_ \-) :row-fn #(assoc % :gender (-> % :gender str))}))

(defn get-by-id [db customer-id localize]
  (try
    (first (j/query db ["select * from customer where id = ?" customer-id]
                       {:identifiers #(.replace % \_ \-)
                        :row-fn #(assoc % :gender (-> % :gender str)
                                          :birthday (-> % :birthday .getTime time-coe/from-long))}))

    (catch Exception e (do (log/error e)
                           (log/error (.getNextException e))
                           (f/fail (localize [:generic/error-loading]))))))

(defn update-customer [db customer user-id localize]
  (try
    (j/update! db :customer {:birthday (new Timestamp (:birthday customer)) :first_name (:first-name customer)
                             :last_name  (:last-name customer) :gender (-> (:gender customer) ->gender)}
               ["id = ? and users_id = ?" (Integer/parseInt (:customer-id customer)) user-id])
    (catch Exception e (do (log/error e)
                           (log/error (.getNextException e))
                           (f/fail (localize [:generic/error-saving]))))))



