(ns appkernel.eventhandling
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]

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
    (let [event-args (second event)
          f (:f handler)]
      (try
        (f db event-args)
        (catch #?(:cljs :default :clj Exception) ex
          (throw (ex-info (str "Event handler " (:name handler) " failed.")
                          {:event event
                           :event-handler handler}
                          ex)))))
    handler
    event))


(defn handle-event
  [db event]
  (tap> [::handle-event event])
  ;; TODO conform event
  (let [event-name (first event)
        handlers (registration/event-handlers-by-event-name db event-name)
        reducer (partial process-event-handler event)]
    (reduce reducer db handlers)))


(defn handle-events
  [db events]
  (reduce handle-event db events))


(def-bindscript ::full-stack
  db          {:stuff #{}}
  event       [:some/event {:param-1 23}]

  result      (handle-event db event)

  handler-1   {:name :some/handler-1
               :event :some/event
               :f (fn [db args] (update db :stuff conj "h1"))}
  handler-2   {:name :some/handler-2
               :event :some/event
               :f (fn [db args] (update db :stuff conj "h2"))}

  db          (registration/reg-event-handler db handler-1)
  db          (registration/reg-event-handler db handler-2)

  db          (handle-event db event)
  stuff       (:stuff db)
  :spec       #(= % #{"h1" "h2"}))
