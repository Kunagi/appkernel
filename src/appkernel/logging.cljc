(ns appkernel.logging
  (:require
   [appkernel.logging-output :as output]))


(defn log-object [o]
  (output/log-object o))


(defn register-tap
  []
  (add-tap log-object))


(defonce registered?
  (do
    (register-tap)
    true))
