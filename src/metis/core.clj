(ns metis.core
  (:use
    [metis.util]
    [clojure.set :only [union]]))

(defn should-run? [record attr options context]
  (let [{:keys [allow-nil allow-blank allow-absence only except if]
         :or {allow-nil false
              allow-blank false
              allow-absence false
              only []
              except []
              if (fn [attrs] true)}} options
        allow-nil (if allow-absence true allow-nil)
        allow-blank (if allow-absence true allow-blank)
        only (flatten [only])
        except (flatten [except])
        value (attr record)
        if-condition (or (:if options) (fn [attrs] true))
        if-not-condition (or (:if-not options) (fn [attrs] false))]
    (not (or
      (and allow-nil (nil? value))
      (and allow-blank (blank? value))
      (and context (not (empty? only)) (not (includes? only context)))
      (and context (not (empty? except)) (includes? except context))
      (not (if-condition record))
      (if-not-condition record)))))

(defprotocol AsString
  (->string [this]))

(extend-protocol AsString
  clojure.lang.Keyword
  (->string [this] (name this))

  java.lang.String
  (->string [this] this)

  clojure.lang.Symbol
  (->string [this] (name this)))

(defn- validator-name [name]
  (symbol (->string name)))

(defn- validator-factory [name]
  (let [name (validator-name name)]
    (or
      (resolve name)
      (throw (Exception. (str "Cound not find validator " name ". Looked in " *ns* " for " name "."))))))

(defn- run-validation [map key validator options context]
  (when (should-run? map key options context)
    (validator map key options)))

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
              next (second validations)]
          (cond
            (map? next)
              (recur (rest (rest validations)) (conj ret [(validator-factory cur) next]))
            (keyword? next)
              (recur (rest validations) (conj ret [(validator-factory cur) {}]))
            (nil? next)
              (recur [] (conj ret [(validator-factory cur) {}]))))))))

(defn -parse
  ([attrs validation args]
   [(-parse-attributes attrs) (-parse-validations [validation args])])
  ([attrs validations]
   [(-parse-attributes attrs) (-parse-validations validations)]))

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
  (let [name (validator-name name)
        validations (vec validations)]
    `(do
      (use 'metis.validators)
      (let [validations# (-expand-validations ~validations)]
        (defn ~name
          ([record# attr# options#] (~name (attr# record#)))
          ([record# context#] (-validate record# validations# context#))
          ([record#] (-validate record# validations# nil)))))))
