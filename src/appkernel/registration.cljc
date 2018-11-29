(ns appkernel.registration
  (:require
   [bindscript.api :refer [def-bindscript]]

   [appkernel.integration :as integration]
   [appkernel.query-responder :as query-responder]))


(defn reg-query-responder
  [db responder]
  (let [responder (query-responder/conform responder)
        query-name (:query responder)]
    (update-in db [:appkernel/query-responders query-name] conj responder)))


(defn def-query-responder
  [responder]
  (integration/update-db #(reg-query-responder % responder)))


(defn responders-by-query-name
  "Provides all responders for a given query name."
  [db query-name]
  (get-in db [:appkernel/query-responders query-name]))
