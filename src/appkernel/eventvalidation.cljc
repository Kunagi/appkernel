(ns appkernel.eventvalidation
  (:require
   [appkernel.registration :as registration]))


(defn validate
  [db event]
  (let [name (:app/event event)
        model (registration/model-by-name db :event name)]
    (when-not model
      (throw (ex-info (str "Invalid event. Event-model " name " does not exist.")
                      {:event event}))))
  event)
