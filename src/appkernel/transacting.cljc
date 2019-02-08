(ns appkernel.transacting
  (:require
   [bindscript.api :refer [def-bindscript]]

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
      (throw (ex-info (str "Missing handler for " command-name)
                      {:command command})))))


(defn- load-aggregate
  [tx]
  ;; TODO
  tx)


(defn- run-command-handler
  [tx]
  (let [f (:f tx)
        command (:command tx)
        command-name (:app/command command)
        command-args (second command)]
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
  (-> (new-tx db command)
      (with-try-catch "load command handler" load-command-handler)
      (with-try-catch "load aggregate" load-aggregate)
      (with-try-catch "run command handler" run-command-handler)
      (with-try-catch "conform events" conform-events)
      (with-try-catch "handle events" handle-events)))


(defn transact!
  [command]
  (tap> [::transact! :error command]))


(def-bindscript ::full-stack
  db      {}
  command {:app/command :do/something :arg "a-1"}

  handler {:command :do/something
           :f (fn [args] [{:app/event :something/done}])}
  db      (registration/reg-command-handler db handler)

  tx      (transact db command))
