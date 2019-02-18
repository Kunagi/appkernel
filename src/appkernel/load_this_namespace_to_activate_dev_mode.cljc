(ns appkernel.load-this-namespace-to-activate-dev-mode
  (:require
   [appkernel.integration :as integration]))

(integration/activate-dev-mode)
