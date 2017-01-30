(ns de.sveri.custman.db.address
  (:require [clojure.spec :as s]
            [cuerdas.core :as str]
            [clojure.java.jdbc :as j]
            [failjure.core :as f]
            [clojure.tools.logging :as log]
            [de.sveri.custman.db.customer :as db-cust])
  (:import (java.sql Timestamp)))

(s/def ::non-empty-string (s/and string? #(not (str/empty? %))))

(s/def ::id number?)
(s/def ::country string?)
(s/def ::state string?)
(s/def ::city string?)
(s/def ::street string?)
(s/def ::house_number string?)
(s/def ::plz string?)
(s/def ::address_type string?)
(s/def ::handy1 string?)
(s/def ::handy2 string?)
(s/def ::landline string?)
(s/def ::email string?)
(s/def ::customer_id number?)
(s/def ::users_id number?)

(s/def ::address (s/keys :opt-un [::id ::country ::state ::city ::street ::house_number ::plz ::address_type
                                  ::customer_id ::users_id ::handy1 ::handy2 ::landline ::email]))
(s/def ::addresses (s/coll-of ::address))


(s/fdef insert-address :args (s/cat :db any? :address ::address :customer ::db-cust/customer :localize fn?))
(defn insert-address [db address customer localize]
  (try
    (j/insert! db :address {:customer_id (:id customer) :country (:country address) :state (:state address)
                            :city (:city address) :street (:street address) :house_number (:house-number address)
                            :plz (:plz address) :handy1 (:handy1 address) :handy2 (:handy2 address)
                            :landline (:landline address) :email (:email address)})
    (catch Exception e (do (log/error e)
                           (f/fail (localize [:generic/error-saving]))))))


(defn get-address-by-customer [db customer-id]
  (j/query db ["select * from address where customer_id = ?" customer-id] {:identifiers #(.replace % \_ \-)}))

