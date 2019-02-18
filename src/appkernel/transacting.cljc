(ns appkernel.transacting
  (:require
   [bindscript.api :refer [def-bindscript]]

   [appkernel.integration :as integration]
   [appkernel.registration :as registration]
   [appkernel.event :as event]
   [appkernel.command :as command]
   [appkernel.eventhandling :as eventhandling]))


(defn- new-tx
  [db command]
  (let [command (command/conform command)]
    {:db db
     :command command}))


(defn- load-command-handler
  [tx]
  (let [db (:db tx)
        command (:command tx)
        command-name (:app/command command)
        handler (registration/command-handler-by-command-name db command-name)]
    (if handler (assoc tx :command-handler handler
                          :f (:f handler))
      (throw (ex-info (str "Missing command handler for command " command-name)
                      {:command command-name
                       :registered-handlers (registration/command-handler-names db)
                       :db (-> db keys)})))))


(defn- load-aggregate
  [tx]
  ;; TODO
  tx)


(defn- run-command-handler
  [tx]
  (let [f (:f tx)
        command (:command tx)
        command-name (:app/command command)
        command-args command]
    (try
      (let [events (f command-args)]
        (assoc tx :events events))
      (catch #?(:cljs :default :clj Exception) ex
        (throw (ex-info (str "Command handler :f for command " command-name " failed.")
                        {:command command}
                        ex))))))


(defn- conform-events
  [tx]
  (update tx :events #(mapv event/conform %)))


(defn- handle-events
  [tx]
  (update tx :db eventhandling/handle-events (:events tx)))


(defn- persist-tx
  [tx]
  (when-let [persist (get-in tx [:db :app/persist-tx-f])]
    (persist tx))
  tx)


(defn- with-try-catch
  [tx message f & args]
  (try
    (apply f (into [tx] args))
    (catch #?(:cljs :default :clj Exception) ex
      (throw (ex-info (str "Transaction failed to " message ".")
                      (select-keys tx [:command :events])
                      ex)))))


(defn transact
  [db command]
  (tap> [::transact command])
  (when-not (:app/db db)
    (throw (ex-info "Given db is not app-db."
                    {:db db})))
  (-> (new-tx db command)
      (with-try-catch "load command handler" load-command-handler)
      (with-try-catch "load aggregate" load-aggregate)
      (with-try-catch "run command handler" run-command-handler)
      (with-try-catch "conform events" conform-events)
      (with-try-catch "handle events" handle-events)
      (with-try-catch "persist tx" persist-tx)))


(defn transact!
  [command]
  (tap> [:inf ::transact! command])
  (if-let [dispatch-f (integration/dispatch-f)]
    (dispatch-f command)
    (integration/update-db
     (fn [db]
       (-> db
           (transact command)
           :db)))))


(def-bindscript ::full-stack
  db      {}
  db      (registration/reg-event-model db {:name :something/done})
  command {:app/command :do/something
           :a1 "a-1"}

  handler {:command :do/something
           :f (fn [args]
                [{:app/event :something/done
                  :command-args args}])}
  db      (registration/reg-command-handler db handler)

  tx      (transact db command))
