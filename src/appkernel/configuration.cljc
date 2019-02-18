(ns appkernel.configuration
  (:require
   [bindscript.api :refer [def-bindscript]]

   [appkernel.utils :as utils]
   [appkernel.configuration-loader :as configuration-loader]
   [appkernel.integration :as integration]))


(defn- merge-into-db!
  [config]
  (tap> [::configure config])
  (integration/update-db
   (fn [db]
     (utils/deep-merge db config))))


(defn configure
  [config]
  (-> config
      (utils/deep-merge (configuration-loader/load-configuration (:app/name config)))
      (merge-into-db!)))
