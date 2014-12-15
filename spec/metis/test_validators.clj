(ns metis.test-validators
  (:require [metis.core :refer [defvalidator]]))

(defvalidator :foreign
  [:first-name :presence])
