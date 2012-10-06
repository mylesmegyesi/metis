(ns metis.test-validators
  (:use [metis.core :only [defvalidator]]))

(defvalidator :foreign
  [:first-name :presence])
