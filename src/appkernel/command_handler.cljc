(ns appkernel.command-handler
  (:require
   [bindscript.api :refer [def-bindscript]]))

(defn conform
  [handler]
  (let [command-name (:command handler)
        f (:f handler)]
    (if-not command-name
      (throw (ex-info (str "Command handler is missing :command.")
                      {:command handler})))
    (if-not (qualified-keyword? command-name)
      (throw (ex-info (str "Command name needs to be a qualified keyword.")
                      {:command handler})))
    (if-not f
      (throw (ex-info (str "Command handler " command-name " is missing :f.")
                      {:handler handler})))
    handler))
