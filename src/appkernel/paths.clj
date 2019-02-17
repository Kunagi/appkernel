(ns appkernel.paths)


(defn app-name []
  (-> "dummy-file"
      java.io.File.
      .getAbsoluteFile
      .getParentFile
      .getName))


(defn configs-dir []
  (let [config-file (java.io.File. "config.edn")]
    (if (.exists config-file)
      (-> config-file .getAbsoluteFile .getParentFile .getPath)
      (str "/etc/" (app-name)))))

