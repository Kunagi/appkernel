(ns appkernel.integration
  "Default implementation for the app-db. Hook-in atoms for integration into
  re-frame.")

(defonce !app-db (atom {}))

(defonce !update-db (atom (fn [f] (swap! !app-db f))))

(defonce !db (atom (fn [] @!app-db)))


(defn update-db
  "Update the app-db by calling `f` on it."
  [f]
  (@!update-db f))


(defn db
  "Get the current app-db."
  []
  (@!db))
