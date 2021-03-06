(ns appkernel.projector
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]

   [appkernel.projecting :as projecting]
   [appkernel.event-handler :as event-handler]))


(defn- assoc-name
  [event-handler event-name projector-name]
  (let [name-namespace (namespace projector-name)
        name-name (str (name projector-name) "." (name event-name))]
    (assoc event-handler :name (keyword name-namespace name-name))))


(defn- provide-f-for-event-handler
  [event-handler]
  (if-let [f (:f event-handler)]
    f
    (if-let [db-f (:db-f event-handler)]
      (fn [projection event]
        (update projection :db db-f event)))))


(defn- projector-event-handler->app-event-handler
  "Convert projector `event-handler` to app event handler."
  [event-handler projector]
  (let [event-name (:event event-handler)
        f (provide-f-for-event-handler event-handler)
        projector-name (:name projector)
        event-handler {:event event-name :f f}]

    ;; :event is required
    (if-not event-name
      (throw (ex-info (str "Projector's event handler is missing :event.")
                      {:projector projector
                       :event-handler event-handler})))

    ;; :f is required
    (if-not f
      (throw (ex-info (str "Projector's event handler is missing :f.")
                      {:projector projector
                       :event-handler event-handler})))

    (-> event-handler

        ;; every event-handler needs an unique name to conform
        (assoc-name event-name projector-name)

        ;; wrapper for global events into projection
        (assoc :f (projecting/new-event-handler-for-projection projector event-handler))

        (event-handler/conform))))


(defn new-query-responder
  [projector]
  (let [name (:name projector)]
    {:name name
     :query name
     :f (fn [db args]
          (let [args (if (nil? args) {} args)]
            [(get-in db [:appkernel/projections name args])]))}))


(defn conform
  [projector]
  (let [projector-name (:name projector)
        event-handlers (:event-handlers projector)]
    (if-not projector-name
      (throw (ex-info (str "Projector is missing :name.")
                      {:projector projector})))
    (if-not event-handlers
      (throw (ex-info (str "Projector is missing :event-handlers.")
                      {:projector projector})))
    (-> projector
        (assoc :event-handlers
               (mapv #(projector-event-handler->app-event-handler % projector)
                     event-handlers))
        (assoc :query-responder (new-query-responder projector)))))
