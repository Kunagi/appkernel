(ns appkernel.query
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]))


(defn conform
  [query]
  (if-not (vector? query)
    (throw (ex-info (str "Query is not a vector.")
                    {:query query})))
  (let [query-name (first query)
        args (second query)
        args (if args args {})]
    (if-not (qualified-keyword? query-name)
      (throw (ex-info (str "Query name needs to be a qualified keyword.")
                      {:query-name query-name
                       :query query})))
    (if-not (map? args)
      (throw (ex-info "Query args needs to be a map."
                      {:query-args args
                       :query query})))
    [query-name args]))
