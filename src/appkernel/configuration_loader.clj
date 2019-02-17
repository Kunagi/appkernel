(ns appkernel.configuration-loader
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [appkernel.paths :as paths]))

(defn load-configuration
  []
  (let [configs-dir (paths/configs-dir)
        file (io/as-file (str configs-dir "/config.edn"))
        path (.getAbsolutePath file)]
    (tap> [::configs-dir configs-dir])
    (if (.exists file)
      (let [config (read-string (slurp file))]
        (tap> [::load-configuration {:file path :config config}])
        config)
      (do
        (tap> [::load-configuration {:file path :config nil}])
        nil))))
