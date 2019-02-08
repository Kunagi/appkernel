(ns appkernel.api
  (:require
   [bindscript.api :refer [def-bindscript]]

   [appkernel.integration :as integration]
   [appkernel.registration :as registration]
   [appkernel.query-responder :as query-responder]
   [appkernel.querying :as querying]
   [appkernel.eventhandling :as eventhandling]
   [appkernel.transacting :as transacting]))


(def dev-mode? integration/dev-mode?)

;;; utils

(defn new-uuid
  []
  (str
   #?(:cljs (random-uuid)
      :clj (java.util.UUID/randomUUID))))

(defn current-time-millis
  []
  #?(:cljs (.getTime (js/Date.))
     :clj (System/currentTimeMillis)))

;;; direct state manipulation


(defn update-db!
  [update-fn & args]
  (tap> ::update-db)
  (integration/update-db #(apply update-fn (into [%] args))))


(defn assoc-in-db!
  [path value]
  (tap> [::assoc-in-db {:path path :value value}])
  (integration/update-db #(assoc-in % path value)))


;;; query

(defn def-query-responder
  [name & {:as responder}]
  (registration/def-query-responder (assoc responder :name name)))


(defn execute-query-sync-and-merge-results
  [db query]
  (querying/execute-query-sync-and-merge-results db query))


(defn q [db query]
  (querying/execute-query-sync-and-merge-results db query))

(defn q-1 [db query]
  (first
   (querying/execute-query-sync-and-merge-results db query)))


;;; event


(defn dispatch
  [event]
  (tap> [::dispatch event])
  (if-let [dispatch-f (integration/dispatch-f)]
    (dispatch-f event)
    (integration/update-db #(eventhandling/handle-event % event)))) ; TODO alternative: command


(defn def-event-handler
  [name & {:as event-handler}]
  (registration/def-event-handler (assoc event-handler :name name)))



;;; command

(defn def-command
  [name & {:as command-handler}]
  (registration/def-command-handler (assoc command-handler :command name)))


;;; projections

(defn def-projector
  [name & {:as projector}]
  (registration/def-projector (assoc projector :name name)))



;;; models


(defn def-event
  [name & {:as model}]
  (registration/def-event-model (assoc model :name name)))


;;; app startup


(def-event :app/started)


(defn start!
  "Starts the application by dispatching the `:appkernel/app-started` event.

  This function must be called from a function in the application's entry
  namespace. This is because the entry namespace must require all other
  used namesapces before calling this function."
  []
  (dispatch {:app/event :app/started}))


;;; integration test


(def-bindscript ::projecting
  db          {:stuff #{}}

  db          (registration/reg-event-model db {:name :some/event-1})
  db          (registration/reg-event-model db {:name :some/event-2})

  event-1     {:app/event :some/event-1 :param-1 23}
  event-2     {:app/event :some/event-2 :param-1 23}

  result      (eventhandling/handle-event db event-1)

  handler-1   {:event :some/event-1
               :f (fn [projection args]
                     (assoc projection :h1 true))}
  handler-2   {:event :some/event-2
               :f (fn [projection args]
                    (assoc projection :h2 true))}

  projector   {:name :some/projection
               :event-handlers [handler-1 handler-2]}

  db          (registration/reg-projector db projector)

  db          (eventhandling/handle-event db event-1)
  db          (eventhandling/handle-event db event-2)

  projection  (first (querying/execute-query-sync-and-merge-results
                      db
                      [:some/projection {}]))

  :spec       #(= % {:projection/name :some/projection
                     :args {}
                     :h1 true
                     :h2 true}))
