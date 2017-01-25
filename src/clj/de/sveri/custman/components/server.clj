(ns de.sveri.custman.components.server
  (:require [com.stuartsierra.component :as component]
            [clojure.tools.logging :as log]
            [immutant.web :as web]
            [hara.io.scheduler :as sched]
            [de.sveri.custman.session :as session])
  (:import (clojure.lang AFunction)))

(defn destroy
  "destroy will be called when your application
   shuts down, put any clean up code here"
  []
  (log/info "custman is shutting down...")
  (sched/shutdown! session/cleanup-job)
  (log/info "shutdown complete!"))

(defn init
  "init will be called once when
   app is deployed as a servlet on
   an app server such as Tomcat
   put any initialization code here"
  [config]
  ;;start the expired session cleanup job
  (sched/start! session/cleanup-job)
  (log/info "\n-=[ custman started successfully"
               (when (= (:env config) :dev) "using the development profile") "]=-"))

(defrecord WebServer [handler config]
  component/Lifecycle
  (start [component]
    (let [handler (:handler handler)
          port (get-in config [:config :port] 3000)
          server (if (= (:env config) :dev)
                   (web/run-dmc handler {:port port})
                   (web/run handler {:port port}))]
      (assoc component :server server)))
  (stop [component]
    (let [server (:server component)]
      (when server (web/stop)))
    component))

(defn new-web-server []
  (map->WebServer {}))
