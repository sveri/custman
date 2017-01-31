(ns de.sveri.custman.core
  (:require [clojure.tools.logging :as log]
            [system.repl :refer [set-init! start]]
            [de.sveri.custman.cljccore :as cljc]
            [de.sveri.custman.components.components :refer [prod-system]]
            [com.stuartsierra.component :as component])
  (:gen-class))

(defn -main [& args]
  (set-init! #'prod-system)
  (start)
  (log/info "server started."))
