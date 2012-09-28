(ns metis.core
  (:use
    [metis.util]
    [clojure.set :only [union]]))

(defn -should-run? [options attr context]
  (let [{:keys [allow-nil allow-blank allow-absence on]
         :or {allow-nil false
              allow-blank false
              allow-absence false
              on [:create :update]}} options
        on (flatten [on])
        allow-nil (if allow-absence true allow-nil)
        allow-blank (if allow-absence true allow-blank)]
    (not (or
      (not (includes? on context))
      (and allow-nil (nil? attr))
      (and allow-blank (blank? attr))))))

(defprotocol AsString
  (->string [this]))

(extend-protocol AsString
  clojure.lang.Keyword
  (->string [this] (name this))

  java.lang.String
  (->string [this] this)

  clojure.lang.Symbol
  (->string [this] (name this)))

(defn validator-name [name]
  (let [name (->string name)
        suffix "-validator"]
    (symbol
      (if (.endsWith name suffix)
        name
        (str name suffix)))))

(defn- validator-factory [name]
  (let [name (validator-name name)
        f (resolve name)]
    (or f (throw (Exception. (str "Cound not find validator " name))))))

(defn -run-validation
  ([record attr validation-name validation-args]
    (-run-validation record attr validation-name validation-args :create))
  ([record attr validation-name validation-args context]
    (let [error (when (-should-run? validation-args (attr record) context) ((validator-factory validation-name) record attr validation-args))]
      (when error
        (let [given-message (:message validation-args)]
          (if  given-message given-message error))))))

(defn -remove-nil [coll]
  (filter #(not (nil? %)) coll))

(defn -run-validations [record attr validations]
  (-remove-nil
    (for [[validation-name validation-args] validations]
      (-run-validation record attr validation-name validation-args))))

(defn- normalize-errors [errors]
  (if (and (= 1 (count errors)) (map? (first errors)))
    (first errors)
    errors))

(defn validate [record validations]
  (reduce
    (fn [errors [attr attr-vals]]
      (let [attr-errors (-run-validations record attr attr-vals)]
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
  (let [validations (-expand-validations validations)
        name (validator-name name)]
    `(do
      (use 'metis.validations)
      (defn ~name
        ([record# attr# options#] (~name (attr# record#)))
        ([record#] (validate record# ~validations))))))
