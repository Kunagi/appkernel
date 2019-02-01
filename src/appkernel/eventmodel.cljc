(ns appkernel.eventmodel)


(defn conform
  [model]
  (if-not (map? model)
    (throw (ex-info (str "Event-model is not a map.")
                    {:eventmodel model})))
  (let [model-name (:name model)]
    (if-not model-name
      (throw (ex-info (str "Event-model is missing :event.")
                      {:eventmodel model})))
    (if-not (qualified-keyword? model-name)
      (throw (ex-info (str "Event-Model name needs to be a qualified keyword.")
                      {:eventmodel model})))
    model))
