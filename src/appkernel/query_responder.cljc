(ns appkernel.query-responder
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]))


(defn conform
  [responder]
  (let [responder-name (:name responder)
        query-name (:query responder)
        f (:f responder)]
    (if-not responder-name
      (throw (ex-info (str "Query responder  is missing :name.")
                      {:responder responder})))
    (if-not query-name
      (throw (ex-info (str "Query responder " responder-name " is missing :query.")
                      {:responder responder})))
    (if-not f
      (throw (ex-info (str "Query responder " responder-name " is missing :f.")
                      {:responder responder})))
    responder))
