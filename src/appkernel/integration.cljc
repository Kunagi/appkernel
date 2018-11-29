(ns appkernel.integration)

(defonce !app-db (atom {}))

(defonce !update-db (atom (fn [f] (swap! !app-db f))))

(defonce !db (atom (fn [] @!app-db)))


(defn update-db
  [f]
  (@!update-db f))


(defn db
  []
  (@!db))
