(ns appkernel.event-model)


(defn conform
  [model]
  (if-not (map? model)
    (throw (ex-info (str "Event-model is not a map.")
                    {:model model})))
  (let [model-name (:name model)]
    (if-not model-name
      (throw (ex-info (str "Event-model is missing :name.")
                      {:model model})))
    (if-not (qualified-keyword? model-name)
      (throw (ex-info (str "Event-Model name needs to be a qualified keyword.")
                      {:model model})))
    (-> model
        (assoc :transient? (or (:transient? model) false))
        (assoc :doc (or (:doc model) (name model-name))))))
