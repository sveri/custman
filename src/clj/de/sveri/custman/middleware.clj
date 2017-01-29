(ns de.sveri.custman.middleware
  (:require [clojure.tools.logging :as log]
            [prone.middleware :as prone]
            [taoensso.tempura :refer [tr] :as tempura]
            [noir-exception.core :refer [wrap-internal-error]]
            [buddy.auth.middleware :refer [wrap-authentication wrap-authorization]]
            [buddy.auth.accessrules :refer [wrap-access-rules]]
            [ring.middleware.anti-forgery :refer [wrap-anti-forgery]]
            [noir.session :as sess]
            [de.sveri.clojure.commons.middleware.util :refer [wrap-trimmings]]
            [clojure-miniprofiler :refer [wrap-miniprofiler in-memory-store]]
            [ring.middleware.transit :refer [wrap-transit-response]]
            [ring.middleware.reload :refer [wrap-reload]]
            [de.sveri.custman.locale :as loc]
            [de.sveri.custman.service.auth :refer [auth-backend]]
            [de.sveri.custman.service.auth :as auth]))

(defonce in-memory-store-instance (in-memory-store))

(def locale-to-date-format {"de" "dd.mm.yyyy"
                            "en" "mm/dd/yyyy"})

(defn add-locale [handler]
  (fn [req]
    (let [accept-language (get-in req [:headers "accept-language"])
          short-languages (or (tempura/parse-http-accept-header accept-language) ["en"])]
      ;(sess/put! :locale (first short-languages))
      ;(sess/put! :date-format (get locale-to-date-format (first short-languages)
      ;                             (get locale-to-date-format "en")))
      (handler (assoc req :localize (partial tr
                                             {:default-locale :en
                                              :dict           loc/local-dict}
                                             short-languages)
                          :locale (get locale-to-date-format (first short-languages)
                                       (get locale-to-date-format "en"))
                          :date-format (get locale-to-date-format (first short-languages)
                                            (get locale-to-date-format "en")))))))

(defn add-req-properties [handler config]
  (fn [req]
    (sess/put! :registration-allowed? (:registration-allowed? config))
    (sess/put! :captcha-enabled? (:captcha-enabled? config))
    (handler req)))

(def development-middleware
  [#(wrap-miniprofiler % {:store in-memory-store-instance})
   #(prone/wrap-exceptions % {:app-namespaces ['de.sveri.custman]})
   wrap-reload])

(defn production-middleware [config]
  [#(add-req-properties % config)
   add-locale
   #(wrap-access-rules % {:rules auth/rules})
   #(wrap-authorization % auth/auth-backend)
   #(wrap-internal-error % :log (fn [e] (log/error e)))
   #(wrap-transit-response % {:encoding :json :opts {}})
   wrap-anti-forgery
   wrap-trimmings])

(defn load-middleware [config]
  (concat (production-middleware config)
          (when (= (:env config) :dev) development-middleware)))
