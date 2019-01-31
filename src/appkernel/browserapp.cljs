(ns appkernel.browserapp
  (:require
   [appkernel.logging]
   [appkernel.api]))

(.log js/console "loading browserapp")

(defn -main []
  (.log js/console "main"))
