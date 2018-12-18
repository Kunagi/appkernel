
(ns appkernel.event
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]))

(defn conform
  [event]
  (let [event-name (:event event)]
    (if-not event-name
      (throw (ex-info (str "Event is missing :event.")
                      {:event event})))
    event))
