(ns appkernel.configuration-loader
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [appkernel.paths :as paths]))

(defn- load-configration-file
  [config-dir]
  (let [file (io/as-file (str config-dir "/config.edn"))
        path (.getAbsolutePath file)]
    (tap> [::config-dir config-dir])
    (if (.exists file)
      (try
        (let [config (read-string (slurp file))]
          (tap> [::load-configuration {:file path :config config}])
          config)
        (catch Exception ex
          (throw (ex-info (str "Failed to read file " (.getAbsolutePath file))
                          {:file file}))))
      (do
        (tap> [::load-configuration {:file path :config nil}])
        {}))))


(defn load-configuration
  [app-name]
  (let [config-dir (paths/config-dir app-name)
        config (load-configration-file config-dir)
        data-dir (if-let [data-dir (:app/data-dir config)]
                   data-dir
                   (-> "user.home" System/getProperty java.io.File. .getPath))]
    (merge
     config
     {:app/config-dir config-dir
      :app/data-dir data-dir})))
