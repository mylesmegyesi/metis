(ns validator.core
  (:use [validator.validations :only [get-validation]]))

(defn validate-attr
  ([attr validator] (validate-attr attr validator {}))
  ([attr validator args] ((get-validation validator) attr args)))

(defn validate [record & validations]
  (loop [[field & more] validations errors {}]
    (if (nil? field)
      errors
      (let [[attr & args] field
            message (when (nil? (attr errors)) (apply validate-attr (attr record) args))]
        (recur more (if message (merge errors {attr message}) errors))))))