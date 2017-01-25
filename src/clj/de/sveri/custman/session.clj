(ns de.sveri.custman.session
  (:require [noir.session :refer [clear-expired-sessions]]
            [hara.io.scheduler :as sched]))

(def cleanup-job
  (sched/add-task
    (sched/scheduler {})
    :entries
    {:handler (fn [_ _] (clear-expired-sessions))
     :schedule "* /30 * * * * *"}))
