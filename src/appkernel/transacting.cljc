(ns appkernel.transacting
  (:require
   [bindscript.api :refer [def-bindscript]]

   [appkernel.registration :as registration]))


(defn- new-tx
  [db command]
  ;; TODO conform command
  {:db db
   :command command})


(defn- load-command-handler
  [tx]
  (let [db (:db tx)
        command (:command tx)
        command-name (first command)
        handler (registration/command-handler-by-command-name db command-name)]
    (if handler (assoc tx :command-handler handler
                          :f (:f handler)))
    (throw (ex-info (str "Missing handler for " command-name)
                    {:command command}))))


(defn- run-command-handler
  [tx]
  (let [f (:f tx)
        command (:command tx)
        command-name (first command)
        command-args (second command)]
    (try
      (let [events (f command-args)]
        (assoc tx :events events))
      (catch #?(:cljs :default :clj Exception) ex
        (throw (ex-info (str "Command handler :f for command " command-name " failed.")
                        {:command command}
                        ex))))))


(defn transact
  [db command]
  (-> (new-tx db command)
      (load-command-handler)
      (run-command-handler)))
