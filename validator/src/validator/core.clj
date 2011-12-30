(ns validator.core
  (:use [validator.validations :only [get-validation]]))

(defn validate-attr
  ([attr validator] (validate-attr attr validator {}))
  ([attr validator args]
    (let [{:keys [message] :or {message "is invalid"}} args
          error ((get-validation validator) attr args)]
      (when error
        message))))

(defn- ensure-attr-vector [errors attr]
  (if (nil? (attr errors))
    (assoc errors attr [])
    errors))

(defn- add-error [errors attr message]
  (let [errors (ensure-attr-vector errors attr)]
    (update-in errors [attr] #(conj % message))))

(defn validate [record & validations]
  (loop [[field & more] validations errors {}]
    (if (nil? field)
      errors
      (let [[attr & args] field
            message (apply validate-attr (attr record) args)
            errors (if message (add-error errors attr message) errors)]
        (recur more errors)))))

(defmacro defvalidator [validator-name & validations]
  `(defn ~validator-name [record#]
    (validate record# ~@validations)))
