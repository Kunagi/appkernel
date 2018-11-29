(ns appkernel.api
  (:require
   [bindscript.api :refer [def-bindscript]]

   [appkernel.registration :as registration]
   [appkernel.query-responder :as query-responder]
   [appkernel.querying]
   [appkernel.command]))


(defn def-query-responder
  [name & {:as responder}]
  (registration/def-query-responder (assoc responder :name name)))
