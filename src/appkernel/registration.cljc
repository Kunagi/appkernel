(ns appkernel.registration
  "Registration of query responders and command handlers."
  (:require
   [bindscript.api :refer [def-bindscript]]

   [appkernel.integration :as integration]
   [appkernel.query-responder :as query-responder]
   [appkernel.event-handler :as event-handler]
   [appkernel.command-handler :as command-handler]))


;;; queries


(defn reg-query-responder
  [db responder]
  (let [responder (query-responder/conform responder)
        query-name (:query responder)]
    (update-in db [:appkernel/query-responders query-name] conj responder)))


(defn def-query-responder
  [responder]
  (integration/update-db #(reg-query-responder % responder)))


(defn query-responders-by-query-name
  "Provides all responders for a given query name."
  [db query-name]
  (get-in db [:appkernel/query-responders query-name]))



;;; events


(defn reg-event-handler
  [db handler]
  (let [handler (event-handler/conform handler)
        event-name (:event handler)]
    (update-in db [:appkernel/event-handlers event-name] conj handler)))


(defn def-event-handler
  [db handler]
  (integration/update-db #(reg-event-handler % handler)))


(defn event-handlers-by-event-name
  "Provides all event handlers for a given event name."
  [db event-name]
  (get-in db [:appkernel/event-handlers event-name]))


;;; commands


(defn reg-command-handler
  [db handler]
  (let [handler (command-handler/conform handler)
        command-name (:command handler)]
    (assoc-in db [:appkernel/command-handlers command-name] handler)))


(defn def-command-handler
  [handler]
  (integration/update-db #(reg-command-handler % handler)))


(defn command-handler-by-command-name
  [db command-name]
  (get-in db [:appkernel/command-handlers command-name]))
