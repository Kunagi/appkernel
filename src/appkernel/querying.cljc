(ns appkernel.querying
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]

   [appkernel.registration :as registration]))


(defn integrate-responder-to-result
  "Runs the responders function and integrates the result."
  [db result responder]
  (let [query-args (get-in result [:query :args])
        f (:f responder)
        response-value (f db query-args)
        response {:value response-value
                  :responder (:name responder)}]
    (update result :responses conj response)))


(defn execute-query-sync
  [db query]
  (let [query-name (first query)
        responders (registration/query-responders-by-query-name db query-name)
        result {:query query
                :responses []}
        reducer (partial integrate-responder-to-result db)]
    (reduce reducer result responders)))


(defn responses-from-result-merged
  [result]
  (reduce
   (fn [response-values response]
     (into response-values (get-in response [:value])))
   []
   (get result :responses)))


(def-bindscript ::full-stack
  db          {}
  query       [:some/query {:param-1 23}]

  result      (execute-query-sync db query)
  all-values  (responses-from-result-merged result)

  responder-1 {:name :some/responder-1
               :query :some/query
               :f (fn [db args] [:a :b :c])}
  responder-2 {:name :some/responder-2
               :query :some/query
               :f (fn [db args] [:x :y :z])}
  db          (registration/reg-query-responder db responder-1)
  db          (registration/reg-query-responder db responder-2)

  result      (execute-query-sync db query)
  all-values  (responses-from-result-merged result)
  :spec       #(= (into #{} %) #{:a :b :c :x :y :z}))
