(ns metis.validator.core
  (:use
    [metis.validator.validations :only [get-validation]]
    [metis.validator.util :only [blank? spear-case]]))

(defn ensure-attr-vector [errors attr]
  (if (nil? (attr errors))
    (assoc errors attr [])
    errors))

(defn add-error [errors attr message]
  (let [errors (ensure-attr-vector errors attr)]
    (update-in errors [attr] #(conj % message))))

(defn validate-attr [record attr validator args]
  (let [{:keys [message allow-nil allow-blank] :or {allow-nil false allow-blank false}} args
        attr-value (attr record)
        error (if (or (and allow-nil (nil? attr-value)) (and allow-blank (blank? attr-value))) nil ((get-validation validator) record attr args))]
    (when error
      (if message message error))))

(defn normalize-attributes [attributes]
  (cond
    (keyword? attributes) [attributes]
    (coll? attributes) attributes))

(defn normalize-validations [validations]
  (let [validations (if (keyword? validations) [validations] validations)]
    (loop [validations validations ret []]
      (if (empty? validations)
        ret
        (let [cur (first validations)
              next (second validations)]
          (cond
            (map? next)
            (recur (rest (rest validations)) (conj ret [cur next]))
            (keyword? next)
            (recur (rest validations) (conj ret [cur {}]))
            (nil? next)
            (recur [] (conj ret [cur {}]))))))))

(defn merge-errors [& errors]
  (apply merge-with #(vec (concat %1 %2)) {} errors))

(defn- validate-normalized [record attrs validations]
  (apply merge-errors
    (filter #(not (nil? %))
      (for [attr attrs [validation args] validations]
        (let [message (validate-attr record attr validation args)]
          (when message
            {attr [message]}))))))

(defn validate
  ([record attrs validation args]
    (validate-normalized record (normalize-attributes attrs) (normalize-validations [validation args])))
  ([record attrs validations]
    (validate-normalized record (normalize-attributes attrs) (normalize-validations validations))))

(defn- get-validator-fn [record fn-name & args]
  `(~fn-name ~record ~@args))

(defmacro merge-validations [record & validations]
  (let [validations (map #(apply get-validator-fn record %) validations)]
    `(apply merge-errors [~@validations])))

(defmacro defvalidator [validator-name & validations]
  `(defn ~validator-name [record#]
    (merge-validations record# ~@validations)))

