(ns de.sveri.custman.core
  (:require [clojure.tools.logging :as log]
            [de.sveri.custman.cljccore :as cljc]
            [de.sveri.custman.components.components :refer [prod-system]]
            [com.stuartsierra.component :as component])
  (:gen-class))

(defn -main [& args]
  (alter-var-root #'prod-system component/start)
  (log/info "server started."))
