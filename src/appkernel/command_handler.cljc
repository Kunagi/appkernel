(ns appkernel.command-handler
  (:require
   [bindscript.api :refer [def-bindscript]]))

(defn conform
  [handler]
  (let [command-name (:command handler)
        f (:f handler)]
    (if-not f
      (throw (ex-info (str "Command handler " command-name " is missing :f.")
                      {:command-handler handler})))
    handler))
