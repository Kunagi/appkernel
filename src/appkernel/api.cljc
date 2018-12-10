(ns appkernel.api
  (:require
   [bindscript.api :refer [def-bindscript]]

   [appkernel.registration :as registration]
   [appkernel.query-responder :as query-responder]
   [appkernel.querying]
   [appkernel.eventhandling]
   [appkernel.transacting :as transacting]))


(defn def-query-responder
  [name & {:as responder}]
  (registration/def-query-responder (assoc responder :name name)))


(defn def-command-handler
  [name & {:as command-handler}]
  (registration/def-command-handler (assoc command-handler :command name)))


(defn !!
  "Execute command and transact."
  [command]
  (transacting/transact! command))
