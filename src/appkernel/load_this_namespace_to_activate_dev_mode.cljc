(ns appkernel.load-this-namespace-to-activate-dev-mode
  (:require
   [appkernel.integration :as integration]))

(tap> ::loading)

(integration/activate-dev-mode)
