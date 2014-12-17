(ns metis.core
  (:require [clojure.set :as set]
            [metis.util :as util]
            [metis.validators]))

(defn should-run? [record attr options context]
  (let [{:keys [allow-nil allow-blank allow-absence only except if]
         :or   {allow-nil     false
                allow-blank   false
                allow-absence false
                only          []
                except        []
                if            (fn [attrs] true)}} options
        allow-nil (if allow-absence true allow-nil)
        allow-blank (if allow-absence true allow-blank)
        only (set (flatten [only]))
        except (set (flatten [except]))
        value (attr record)
        if-condition (or (:if options) (fn [attrs] true))
        if-not-condition (or (:if-not options) (fn [attrs] false))]
    (not (or
           (and allow-nil (nil? value))
           (and allow-blank (util/blank? value))
           (and context (not (empty? only)) (not (only context)))
           (and context (not (empty? except)) (except context))
           (not (if-condition record))
           (if-not-condition record)))))

(defn- validator-name [name]
  (symbol (clojure.core/name name)))

(defn- validator-factory [name]
  (let [name (validator-name name)]
    (or
      #+clj (or (ns-resolve (the-ns 'metis.validators) name) (resolve name))
      #+cljs (or (js* "eval(~{})" (str "metis.validators." name)) (js* "eval(~{})" name))
      (throw (#+clj Exception. #+cljs js/Error. (str "Cound not find validator " name))))))

(defn- run-validation [map key validate-fn options context]
  (when (should-run? map key options context)
    (validate-fn map key options)))

(defn- run-validations [map key validations context]
  (reduce
    (fn [errors [validator options]]
      (if-let [error (run-validation map key validator options context)]
        (if (not (empty? error))
          (conj errors (or (:message options) error))
          errors)
        errors))
    []
    validations))

(defn- normalize-errors [errors]
  (if (and (= 1 (count errors)) (map? (first errors)))
    (first errors)
    errors))

(defn -validate [record validations context]
  (reduce
    (fn [errors [attr attr-vals]]
      (let [attr-errors (run-validations record attr attr-vals context)]
        (if (every? empty? attr-errors)
          errors
          (assoc errors attr (normalize-errors attr-errors)))))
    {}
    validations))

(defn -parse-attributes [attributes]
  (flatten [attributes]))

(defn -parse-validations [validations]
  (let [validations (flatten [validations])]
    (loop [validations validations ret []]
      (if (empty? validations)
        ret
        (let [cur (first validations)
              next (second validations)
              validation (validator-factory cur)
              ret (conj ret [validation next])]
          (cond
            (map? next) (recur (rest (rest validations)) ret)
            (keyword? next) (recur (rest validations) ret)
            (nil? next) (recur [] ret)))))))

(defn -parse
  ([attrs validation args]
   [(-parse-attributes attrs) (-parse-validations [validation args])])
  ([attrs validations]
   [(-parse-attributes attrs) (-parse-validations validations)]))

(defn -merge-validations [validations]
  (apply merge-with set/union {} validations))

(defn -expand-validation [validation]
  (let [[attributes validations] (apply -parse validation)]
    (-merge-validations
      (for [attr attributes validation validations]
        {attr #{validation}}))))

(defn -expand-validations [validations]
  (-merge-validations (map -expand-validation validations)))

(defn validator [& validations]
  (let [validations (vec validations)
        validations (-expand-validations validations)]
    (fn
      ([record attr context] (-validate (attr record) validations context))
      ([record context] (-validate record validations context))
      ([record] (-validate record validations nil)))))

#+clj
(defmacro defvalidator [name & validations]
  (let [name (validator-name name)]
    `(def ~name (apply validator ~(vec validations)))))
