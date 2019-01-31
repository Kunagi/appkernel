(ns appkernel.lifecycle
  "Starting, stopping, reloading."
  (:require
   [appkernel.api :refer [!! def-command-handler]]))


(defn start!
  []
  (!! [:appkernel/handle-events {:events [[:appkernel/app-started {}]]}]))
