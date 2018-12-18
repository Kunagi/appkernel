
(ns appkernel.event-handler
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]))


(defn conform
  [handler]
  (let [handler-name (:name handler)
        event-name (:event handler)
        f (:f handler)]
    (if-not event-name
      (throw (ex-info (str "Event handler " handler-name " is missing :event.")
                      {:handler handler})))
    (if-not f
      (throw (ex-info (str "Event handler " handler-name " is missing :f.")
                      {:handler handler})))
    handler))
