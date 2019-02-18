(ns appkernel.tx-store
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]

   [appkernel.integration :as integration]
   [appkernel.registration :as registration]
   [appkernel.paths :as paths]))


(defn new-store
  [path]
  (tap> [:inf ::load path])
  (let [offset-path (str path "/offset")
        offset-file (io/as-file offset-path)
        offset (if-not (.exists offset-file)
                 0
                 (edn/read-string (slurp offset-file)))
        store {:path path
               :offset offset}]
    (tap> [::loaded store])
    store))


(defn num->path
  [num]
  (let [s (-> num (+ 1000000) str)
        s2 (- (.length s) 3)
        s1 (- (.length s) 6)]
    (str (.substring s 0 s1) "/" (.substring s s1 s2) "/" (.substring s s2))))


(defn filter-non-transient-events
  [db events]
  (into []
        (filter
         #(let [event-name (:app/event %)
                event-model (registration/model-by-name db :event event-name)]
            (not (:transient? event-model))))
        events))

(defn persist-tx
  [store {:as tx :keys [db events]}]
  (let [tx-num (-> store :offset inc)
        tx-data {:num tx-num
                 :events (filter-non-transient-events db (:events tx))
                 :time (System/currentTimeMillis)}
        tx-path (str (:path store) "/tx/" (num->path tx-num) ".edn")
        offset-path (str (:path store) "/offset")]
    (tap> [:inf ::persist-tx {:num tx-num :file tx-path}])
    (let [tx-file (io/as-file tx-path)
          offset-file (io/as-file offset-path)]
      (-> tx-file .getParentFile .mkdirs)
      (spit tx-file (pr-str tx-data))
      (spit offset-file tx-num))
    (-> store
        (assoc :offset tx-num))))


(defn load-and-get-persist-tx-f
  [path]
  (let [!store (atom (new-store path))]
    (fn [tx]
      (swap! !store persist-tx tx))))


(defn install! []
  (integration/update-db
   (fn [db]
     (let [path (str (:app/data-dir db) "/tx-log")
           persist-tx-f (load-and-get-persist-tx-f path)]
       (assoc db :app/persist-tx-f persist-tx-f)))))
