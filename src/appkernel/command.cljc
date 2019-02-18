(ns appkernel.command
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]))


(defn- conform-
  [command]
  (if-not (map? command)
    (throw (ex-info (str "Command is not a map.")
                    {:command command})))
  (let [command-name (:app/command command)]
    (if-not command-name
      (throw (ex-info (str "Command is missing :app/command.")
                      {:command command})))
    (if-not (qualified-keyword? command-name)
      (throw (ex-info (str "Command name needs to be a qualified keyword.")
                      {:command command})))
    command))


(defn- new-event-command
  [event]
  {:app/command :app/events
   :events [event]})


(defn- is-event?
  [command-or-event]
  (and
    (not (:app/command command-or-event))
    (:app/event command-or-event)))


(defn conform
  [command]
  ;; Support events as commands
  (if (is-event? command)
    (new-event-command command)
    (conform- command)))
