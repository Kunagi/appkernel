(ns appkernel.paths)

(defn configs-dir
  [app-name]
  (let [config-file (java.io.File. "config.edn")]
    (if (.exists config-file)
      (-> config-file .getAbsoluteFile .getParentFile .getPath)
      (str "/etc/" app-name))))
