(ns appkernel.configuration-loader
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [appkernel.paths :as paths]))

(defn load-configuration
  [app-name]
  (let [config-dir (paths/config-dir app-name)
        data-dir (paths/data-dir app-name)
        file (io/as-file (str config-dir "/config.edn"))
        path (.getAbsolutePath file)
        config {:app/config-dir config-dir
                :app/data-dir data-dir}]
    (tap> [::config-dir config-dir])
    (if (.exists file)
      (try
        (let [config-edn (read-string (slurp file))]
          (tap> [::load-configuration {:file path :config config-edn}])
          (merge config-edn config))
        (catch Exception ex
          (throw (ex-info (str "Failed to read file " (.getAbsolutePath file))
                          {:file file}))))
      (do
        (tap> [::load-configuration {:file path :config nil}])
        config))))
