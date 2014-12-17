(ns metis.test-validators
  (#+clj :require #+cljs :require-macros
                  [metis.core :refer [defvalidator]])
  (:require [metis.core]))

(defvalidator :foreign
  [:first-name :presence])
