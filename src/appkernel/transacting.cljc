(ns appkernel.transacting
  (:require
   [bindscript.api :refer [def-bindscript]]

   [appkernel.registration :as registration]
   [appkernel.event :as event]))


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
        command-name (first command)
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

(defn- store-tx
  [tx]
  ;; TODO
  tx)


(defn transact
  [db command]
  (-> (new-tx db command)
      (load-command-handler)
      (load-aggregate)
      (run-command-handler)
      (conform-events)
      (store-tx)))


(defn transact!
  [command])


(def-bindscript ::full-stack
  db      {}
  command [:do/something {:arg "a-1"}]

  handler {:command :do/something
           :f (fn [args] [{:event :something/done}])}
  db      (registration/def-command-handler handler)

  tx      (transact db command))
