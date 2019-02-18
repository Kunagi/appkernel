(ns appkernel.builtin-commands
  (:require
   [appkernel.registration :as registration]))


(registration/def-command-handler
  {:command :app/passthrough-events
   :f (fn [args]
        (:events args))})
