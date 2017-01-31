(ns de.sveri.custman.locale
  (:require [de.sveri.clojure.commons.files.edn :as comm-edn]))

(def local-dict
  {:de (comm-edn/from-edn "i18n/de.edn")
   :en (comm-edn/from-edn "i18n/en.edn")})


(def locale-to-datepicker-format {"de" "dd.mm.yyyy"
                                  "en" "mm/dd/yyyy"})

(def locale-to-java-date-format {"de" "dd.MM.yyyy"
                                 "en" "MM/dd/yyyy"})

(defn get-datepicker-format [language]
  (get locale-to-datepicker-format language (get locale-to-datepicker-format "en")))

(defn get-date-java-format [language]
  (get locale-to-java-date-format language (get locale-to-java-date-format "en")))


