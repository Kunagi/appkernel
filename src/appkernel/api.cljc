(ns appkernel.api
  (:require
   [bindscript.api :refer [def-bindscript]]

   [appkernel.registration :as registration]
   [appkernel.query-responder :as query-responder]
   [appkernel.querying :as querying]
   [appkernel.eventhandling :as eventhandling]
   [appkernel.transacting :as transacting]))

;;; query

(defn def-query-responder
  [name & {:as responder}]
  (registration/def-query-responder (assoc responder :name name)))


(defn execute-query-sync-and-merge-results
  [db query]
  (querying/execute-query-sync-and-merge-results db query))


;;; command

(defn def-event-handler
  [name & {:as event-handler}]
  (registration/def-event-handler (assoc event-handler :name name)))


(defn def-command-handler
  [name & {:as command-handler}]
  (registration/def-command-handler (assoc command-handler :command name)))


(defn !!
  "Execute command and transact."
  [command]
  (transacting/transact! command))


;;; projections

(defn def-projector
  [name & {:as projector}]
  (registration/def-projector (assoc projector :name name)))


(def-bindscript ::projecting
  db          {:stuff #{}}
  event-1     [:some/event-1 {:param-1 23}]
  event-2     [:some/event-2 {:param-1 23}]

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

  :spec       #(= % {:name :some/projection
                     :args {}
                     :h1 true
                     :h2 true}))
