(ns appkernel.registration
  "Registration of query responders and command handlers."
  (:require
   [bindscript.api :refer [def-bindscript]]

   [appkernel.integration :as integration]
   [appkernel.query-responder :as query-responder]
   [appkernel.event-handler :as event-handler]
   [appkernel.projector :as projector]
   [appkernel.command-handler :as command-handler]
   [appkernel.eventmodel :as eventmodel]))


;;; models

(defn- reg-model
  [db model type conform-f]
  (let [model (conform-f model)
        model-name (:name model)]
    (assoc-in db [:appkernel/models type model-name] model)))


(defn model-by-name
  [db type model-name]
  (get-in db [:appkernel/models type model-name]))


(defn reg-eventmodel
  [db model]
  (reg-model db model :event eventmodel/conform))


(defn def-eventmodel
  [model]
  (tap> [::def-eventmodel (:name model)])
  (integration/update-db #(reg-eventmodel % model)))



;;; queries


(defn reg-query-responder
  [db responder]
  (let [responder (query-responder/conform responder)
        responder-name (:name responder)]
    (assoc-in db [:appkernel/query-responders responder-name] responder)))


(defn def-query-responder
  [responder]
  (tap> [::def-query-responder (:name responder)])
  (integration/update-db #(reg-query-responder % responder)))


(defn query-responders-by-query-name
  "Provides all responders for a given query name."
  [db query-name]
  (->> (get db :appkernel/query-responders)
       (vals)
       (filter #(= query-name (:query %)))))


;;; events


(defn reg-event-handler
  [db handler]
  (let [handler (event-handler/conform handler)
        handler-name (:name handler)]
    (assoc-in db [:appkernel/event-handlers handler-name] handler)))


(defn def-event-handler
  [handler]
  (tap> [::def-event-handler (:name handler)])
  (integration/update-db #(reg-event-handler % handler)))


(defn event-handlers-by-event-name
  "Provides all event handlers for a given event name."
  [db event-name]
  (->> (get db :appkernel/event-handlers)
       (vals)
       (filter #(= event-name (:event %)))))


;;; projections


(defn- reg-projector-event-handlers
  [db projector]
  (reduce reg-event-handler db (:event-handlers projector)))


(defn reg-projector
  [db projector]
  (let [projector (projector/conform projector)
        projector-name (:name projector)]
    (-> db
        (assoc-in [:appkernel/projectors projector-name] projector)
        (reg-projector-event-handlers projector)
        (reg-query-responder (:query-responder projector)))))


(defn def-projector
  [projector]
  (tap> [::def-projector (:name projector)])
  (integration/update-db #(reg-projector % projector)))


;;; commands


(defn reg-command-handler
  [db handler]
  (let [handler (command-handler/conform handler)
        command-name (:command handler)]
    (assoc-in db [:appkernel/command-handlers command-name] handler)))


(defn def-command-handler
  [handler]
  (tap> [::def-command-handler (:name handler)])
  (integration/update-db #(reg-command-handler % handler)))


(defn command-handler-by-command-name
  [db command-name]
  (get-in db [:appkernel/command-handlers command-name]))


