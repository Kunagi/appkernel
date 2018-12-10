(ns appkernel.lifecycle
  "Starting, stopping, reloading."
  (:require
   [appkernel.api :refer [!! def-command-handler]]))


(def-command-handler :appkernel/fire-events
  :f (fn [args]
       (:events args)))


(defn start!
  []
  (!! [:appkernel/handle-events {:events [[:appkernel/app-started {}]]}]))
