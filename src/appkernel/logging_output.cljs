(ns appkernel.logging-output)


(defn log-object
  [o]
  (.log js/console "tap>" o))

