(ns metis.core
  (:use
    [metis.util]
    [clojure.set :only [union]]))

(defn should-run? [options attr context]
  (let [{:keys [allow-nil allow-blank allow-absence only except]
         :or {allow-nil false
              allow-blank false
              allow-absence false}} options
        allow-nil (if allow-absence true allow-nil)
        allow-blank (if allow-absence true allow-blank)]
    (not (or
      (and allow-nil (nil? attr))
      (and allow-blank (blank? attr))
      (and context only (not (includes? only context)))
      (and context except (includes? except context))))))

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

(defn- run-validation [record attr validator options context]
  (let [error (when (should-run? options (attr record) context) (validator record attr options))]
    (when error
      (let [given-message (:message options)]
        (if  given-message given-message error)))))

(defn- remove-nil [coll]
  (filter #(not (nil? %)) coll))

(defn- run-validations [record attr validations context]
  (remove-nil
    (for [[validator options] validations]
      (run-validation record attr validator options context))))

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
  (let [name (validator-name name)]
    `(do
      (use 'metis.validators)
      (let [validations# (-expand-validations '~validations)]
        (defn ~name
          ([record# attr# options#] (~name (attr# record#)))
          ([record# context#] (-validate record# validations# context#))
          ([record#] (-validate record# validations# nil))
          )))))
