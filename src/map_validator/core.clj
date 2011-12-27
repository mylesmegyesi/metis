(ns map-validator.core
  (:use [map-validator.validations :only [get-validation]]))

(defn validate-attr
  ([attr validator] (validate-attr attr validator {}))
  ([attr validator args] ((get-validation validator) attr args)))