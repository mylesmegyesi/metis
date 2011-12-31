(ns metis.model.core
  (:use [clojure.string :only [lower-case]]
        [metis.validator.core :only [defvalidator]]))

(defn- keyword->symbol [key]
  (symbol (name key)))

(defn- attributes [model-name params]
  (prn params)
  (let [record-name (symbol model-name)
        constructor-name (symbol (lower-case (str "new-" model-name)))
        attrs (vec (map keyword->symbol params))
        ctor-helper (symbol (str constructor-name "-helper"))]
    `(do
      (defrecord ~record-name ~attrs)
      (defn- ~ctor-helper ~attrs
        (new ~record-name ~@attrs))
      (defn ~constructor-name [attrs#]
        (apply ~ctor-helper (map (fn [attr#] (attr# attrs#)) [~@params]))))))

(defn- validations [model-name params]
  (let [validator-name (symbol (lower-case (str model-name "-errors")))]
    `(defvalidator ~validator-name ~@params)))

(defn- evaluate-option [option model-name params]
  (let [option-fn (ns-resolve 'metis.model.core (keyword->symbol option))]
    (when option-fn
      (option-fn model-name params))))

(defmacro defmodel [model-name & opts]
  (loop [[[option & params] & more] opts forms []]
    (if (nil? option)
      `(do
        ~@forms)
      (recur more (conj forms (evaluate-option option model-name params))))))

