(ns appkernel.integration
  "Default implementation for the app-db. Hook-in atoms for integration into
  re-frame."
  (:require
   [appkernel.logging]))

(def empty-app-db {:app/db :app/db})

(defonce !dev-mode (atom false))
(defonce !app-db #?(:cljs (atom empty-app-db)
                    :clj (agent
                          empty-app-db
                          :error-mode :continue
                          :error-handler (fn [agent ex]
                                           (tap> [:err ::update-db-failed ex])))))

(defonce !dispatch-f (atom nil))


(defonce !update-db (atom (fn [f]
                            #?(:cljs (swap! !app-db f)
                               :clj (send-off !app-db f)))))


(defonce !db-f (atom (fn [] @!app-db)))


;; TODO (defn integrate ) -> move all content from app-db to new app-db

(defn integrate!
  "Installs `db-fn` and `update-db-fn`, returns current state of db."
  [{:keys [db-f update-db-f dispatch-f]}]
  (tap> ::integrate!)
  (let [db (@!db-f)]
    (reset! !db-f db-f)
    (reset! !update-db update-db-f)
    (reset! !dispatch-f dispatch-f)
    db))


(defn update-db
  "Update the app-db by calling `f` on it."
  [f]
  (@!update-db f))


(defn db
  "Get the current app-db."
  []
  (@!db-f))


(defn dev-mode?
  []
  @!dev-mode)


(defn activate-dev-mode
  []
  (tap> ::activate-dev-mode)
  (reset! !dev-mode true)
  (update-db #(assoc % :dev-mode? true)))


(defn dispatch-f
  []
  @!dispatch-f)

