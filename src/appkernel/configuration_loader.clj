(ns appkernel.configuration-loader
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]))

(defn load-configuration
  []
  (let [file (io/as-file "config.edn")
        path (.getAbsolutePath file)]
    (if (.exists file)
      (let [config (read-string (slurp file))]
        (tap> [::load-configuration {:file path :config config}])
        config)
      (do
        (tap> [::load-configuration {:file path :config nil}])
        nil))))
