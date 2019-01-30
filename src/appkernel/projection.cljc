(ns appkernel.projection
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]))


(defn conform
  [projection]
  (let [projection-name (:projection projection)]
    (if-not projection-name
      (throw (ex-info (str "Projection is missing :projection.")
                      {:projection projection})))
    projection))


