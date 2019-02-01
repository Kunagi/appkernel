(ns appkernel.projecting
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]

   [appkernel.projection :as projection]))

(defn new-projection
  [projector]
  (let [name (:name projector)
        args {}
        init-f (:init-projection-f projector)
        projection {:name name
                    :args {}}]
    (if init-f
      (init-f projection)
      projection)))


(defn- assoc-projection
  [transaction]
  (let [projector (:projector transaction)
        name (:name projector)
        args {}
        projection (get-in transaction [:db :appkernel/projections name args])
        projection (if projection projection (new-projection projector))]
    (assoc transaction :projection projection)))


(defn- project-event
  [projection args projection-event-handler]
  (-> projection
      (projection-event-handler args)))


(defn- project
  [transaction]
  (let [projection-event-handler (:projection-event-handler transaction)
        f (:f projection-event-handler)
        event-args (:event-args transaction)]
    (update transaction :projection f event-args)))


(defn- assoc-projection-in-db
  [transaction]
  (let [db (:db transaction)
        projection (:projection transaction)
        projection-name (:name projection)
        projection-args (:args projection)]
    (assoc-in transaction
              [:db :appkernel/projections projection-name projection-args]
              projection)))


(defn handle-event
  [projector projection-event-handler db event-args]
  (-> {:projector projector
       :projection-event-handler projection-event-handler
       :db db
       :event-args event-args}
      (assoc-projection)
      (project)
      (assoc-projection-in-db)
      :db))


(defn new-event-handler-for-projection
  [projector projection-event-handler]
  (fn [db args]
    (handle-event projector projection-event-handler db args)))


