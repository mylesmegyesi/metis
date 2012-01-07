(ns metis.validator.core
  (:use
    [metis.validator.validations :only [get-validation]]
    [metis.validator.util :only [blank? spear-case in?]]
    [clojure.set :only [union]]))



(defn -run-validation
  ([record attr validation-name validation-args]
    (-run-validation record attr validation-name validation-args :create))
  ([record attr validation-name validation-args context]
    (let [{:keys [message allow-nil allow-blank on] :or {allow-nil false allow-blank false on [:create :update]}} validation-args
          on (flatten [on])
          attr-value (attr record)
          error (if (or (and allow-nil (nil? attr-value)) (and allow-blank (blank? attr-value)) (not (in? context on))) nil ((get-validation validation-name) record attr validation-args))]
      (when error
        (if message message error)))))

(defn -remove-nil [coll]
  (filter #(not (nil? %)) coll))

(defn -run-validations [record attr validations]
  (-remove-nil
    (for [[validation-name validation-args] validations]
      (-run-validation record attr validation-name validation-args))))

(defn -merge-errors [errors]
  (apply merge {} errors))

(defn -remove-empty-values [map-to-filter]
  (select-keys map-to-filter (for [entry map-to-filter :when (not (empty? (val entry)))] (key entry))))

(defn validate [record validations]
  (-remove-empty-values
    (-merge-errors
      (for [validation validations]
        (let [attr (key validation)
              attr-vals (val validation)]
          {attr (-run-validations record attr attr-vals)})))))

(defn -parse-attributes [attributes]
  (flatten [attributes]))

(defn -parse-validations [validations]
  (let [validations (flatten [validations])]
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

(defn -parse
  ([attrs validation args] [(-parse-attributes attrs) (-parse-validations [validation args])])
  ([attrs validations] [(-parse-attributes attrs) (-parse-validations validations)]))

(defn -merge-validations [validations]
  (apply merge-with union {} validations))

(defn -expand-validation [validation]
  (let [[attributes validations] (apply -parse validation)]
    (-merge-validations
      (for [attr attributes validation validations]
        {attr #{validation}}))))

(defn -expand-validations [validations]
  (-merge-validations (map -expand-validation validations)))

(defmacro defvalidator [name & validations]
  (let [validations (-expand-validations validations)]
    `(defn ~name [record#]
      (validate record# ~validations))))

;(defn my-validator [record]
;  (validate record {:first-name [[:presence {:allow-blank true}] [:presence {:allow-nil true}]] :zipcode [[:presence {:allow-blank true}]]}))
; {:first-name ["is not" "is something else"]}