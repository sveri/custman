(ns custman.dev
  (:require [schema.core :as s]
            [de.sveri.custman.core :as core]))

(s/set-fn-validation! true)

(enable-console-print!)

(defn main [] (core/main))
