(ns appkernel.integration
  "Default implementation for the app-db. Hook-in atoms for integration into
  re-frame.")

(defonce !app-db (atom {}))


(defonce !update-db (atom (fn [f] (swap! !app-db f))))

(defonce !db (atom (fn [] @!app-db)))


;; TODO (defn integrate ) -> move all content from app-db to new app-db

(defn integrate!
  "Installs `db-fn` and `update-db-fn`, returns current state of db."
  [db-fn update-db-fn]
  (let [db (@!db)]
    (reset! !db db-fn)
    (reset! !update-db update-db-fn)
    db))


(defn update-db
  "Update the app-db by calling `f` on it."
  [f]
  (@!update-db f))


(defn db
  "Get the current app-db."
  []
  (@!db))
