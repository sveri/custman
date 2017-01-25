(ns de.sveri.custman.user
  (:require [system.repl :refer [set-init! start reset]]
            [de.sveri.custman.components.components :refer [dev-system]]))

(defn start-dev-system []
  (start))

(set-init! #'dev-system)
