(ns de.sveri.custman.components.components
  (:require
    [com.stuartsierra.component :as component]
    [de.sveri.custman.components.selmer :as selm]
    [de.sveri.custman.components.server :refer [new-web-server]]
    [de.sveri.custman.components.handler :refer [new-handler]]
    [de.sveri.custman.components.config :as c]
    [de.sveri.custman.components.db :refer [new-db]]))


(defn dev-system []
  (component/system-map
    :config (c/new-config (c/prod-conf-or-dev))
    :selmer (selm/new-selmer false)
    :db (component/using (new-db) [:config])
    :handler (component/using (new-handler) [:config :db])
    :web (component/using (new-web-server) [:handler :config])))


(defn prod-system []
  (component/system-map
    :config (c/new-config (c/prod-conf-or-dev))
    :selmer (selm/new-selmer true)
    :db (component/using (new-db) [:config])
    :handler (component/using (new-handler) [:config :db])
    :web (component/using (new-web-server) [:handler :config])))
