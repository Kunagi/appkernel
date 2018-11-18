(ns appkernel.api
  (:require
   [bindscript.api :refer [def-bindscript]]))

(defn my-function [a b]
  (+ a b))

(def-bindscript ::my-function
  ret (my-function 2 3))
