(ns appkernel.command
  (:require
   [clojure.spec.alpha :as s]
   [bindscript.api :refer [def-bindscript]]))

;;; specs

(s/def :command/name qualified-keyword?)

(def-bindscript ::specs
  command-name :do/something
  :spec :command/name)
