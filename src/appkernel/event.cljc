(ns appkernel.event
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]))

(defn conform
  [event]
  (if-not (map? event)
    (throw (ex-info (str "Event is not a map.")
                    {:event event})))
  (let [event-name (:app/event event)]
    (if-not event-name
      (throw (ex-info (str "Event is missing :app/event.")
                      {:event event})))
    (if-not (qualified-keyword? event-name)
      (throw (ex-info (str "Event name needs to be a qualified keyword.")
                      {:event event})))
    event))
