(ns appkernel.eventhandling
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]

   [appkernel.event :as event]
   [appkernel.eventvalidation :as eventvalidation]
   [appkernel.registration :as registration]))


(defn validate-event-handler-result
  [db handler event]
  (if-not (map? db)
    (throw (ex-info (str "Event handler " (:name handler)
                         " didn't return db.")
                    {:event event
                     :result db}))
    db))


(defn process-event-handler
  [event db handler]
  (validate-event-handler-result
    (let [f (:f handler)]
      (try
        (f db event)
        (catch #?(:cljs :default :clj Exception) ex
          (throw (ex-info (str "Event handler " (:name handler) " failed.")
                          {:event event
                           :event-handler handler}
                          ex)))))
    handler
    event))


(defn- handle-event-
  [db event]
  (let [event-name (:app/event event)
        handlers (registration/event-handlers-by-event-name db event-name)
        reducer (partial process-event-handler event)]
    (reduce reducer db handlers)))


(defn- validate-event
  [db event]
  (let [event (event/conform event)
        _ (eventvalidation/validate db event)]
    event))


(defn handle-event
  [db event]
  (tap> [::handle-event event])
  (handle-event- db (validate-event db event)))


(defn handle-events
  [db events]
  (tap> [::handle-events events])
  (reduce handle-event
          db
          (map (partial validate-event db)
               events)))


(def-bindscript ::full-stack

  db          {:stuff #{}}
  db          (registration/reg-eventmodel db {:name :some/event})

  event       {:app/event :some/event
               :param-1 23
               :param-2 42}

  result      (handle-event db event)

  handler-1   {:name :some/handler-1
               :event :some/event
               :f (fn [db event] (update db :stuff conj (:param-1 event)))}
  handler-2   {:name :some/handler-2
               :event :some/event
               :f (fn [db event] (update db :stuff conj (:param-2 event)))}

  db          (registration/reg-event-handler db handler-1)
  db          (registration/reg-event-handler db handler-2)

  db          (handle-event db event)
  stuff       (:stuff db)
  :spec       #(= % #{23 42}))
