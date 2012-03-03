(ns metis.validations
  (:use [metis.util]))

(defn with [record attr {validator :validator}]
  (if validator
    (when-not (validator record)
      "is invalid")
    (throw (Exception. "Validator not given."))))

(defn presence [record attr {}]
  (let [attr-value (attr record)]
    (when-not (present? attr-value)
      "must be present")))

(defn acceptance [record attr {:keys [accept] :or {accept "1"}}]
  (let [attr-value (attr record)]
    (when (not= attr-value accept)
      "must be accepted")))

(defn confirmation [record attr args]
  (let [{:keys [confirm] :or {confirm (keyword (str (keyword->str attr) "-confirmation"))}} args
        attr-value (attr record)
        confirm-value (confirm record)]
    (when (not= attr-value confirm-value)
      "doesn't match confirmation")))

(defn numericality [record attr args]
  (let [attr-value (attr record)
        {:keys [only-integer greater-than greater-than-or-equal-to equal-to not-equal-to less-than less-than-or-equal-to odd even in not-in]
         :or {only-integer false
              odd false
              even false}} args
        greater-than-f (when greater-than (float greater-than))
        greater-than-or-equal-to-f (when greater-than-or-equal-to (float greater-than-or-equal-to))
        equal-to-f (when equal-to (float equal-to))
        not-equal-to-f (when not-equal-to (float not-equal-to))
        less-than-f (when less-than (float less-than))
        less-than-or-equal-to-f (when less-than-or-equal-to (float less-than-or-equal-to))
        in (when in (map float in))
        not-in (when not-in (map float not-in))
          {:keys [is-not-an-int is-not-a-number is-not-greater-than is-not-greater-than-or-equal-to is-not-equal-to is-equal-to is-not-less-than is-not-less-than-or-equal-to is-not-odd is-not-even is-not-in is-in]
           :or {is-not-an-int "must be an integer"
                is-not-a-number "must be a number"
                is-not-greater-than "must be greater than %s"
                is-not-greater-than-or-equal-to "must be greater than or equal to %s" 
                is-not-equal-to "must be equal to %s"
                is-equal-to "must not be equal to %s"
                is-not-less-than "must be less than %s"
                is-not-less-than-or-equal-to "must be less than or equal to %s"
                is-not-odd "must be odd"
                is-not-even "must be even"
                is-not-in "must be included in the list"
                is-in "must not be included in the list"}} args
        n-int (when (string? attr-value) (str->int attr-value))
        n-float (cond
          (string? attr-value) (str->float attr-value)
          (float? attr-value) attr-value
          :else nil)]
    (or
      (when (and only-integer (not (integer? n-int))) is-not-an-int)
      (when-not (number? n-float) is-not-a-number)
      (when (and (present? greater-than-f) (not (> n-float greater-than-f))) (format is-not-greater-than greater-than))
      (when (and (present? greater-than-or-equal-to-f) (not (>= n-float greater-than-or-equal-to-f))) (format is-not-greater-than-or-equal-to greater-than-or-equal-to))
      (when (and (present? equal-to-f) (not= n-float equal-to-f)) (format is-not-equal-to equal-to))
      (when (and (present? not-equal-to-f) (= n-float not-equal-to-f)) (format is-equal-to not-equal-to))
      (when (and (present? less-than-f) (not (< n-float less-than-f))) (format is-not-less-than less-than))
      (when (and (present? less-than-or-equal-to-f) (not (<= n-float less-than-or-equal-to-f))) (format is-not-less-than-or-equal-to less-than-or-equal-to))
      (when (and odd (not (odd? (int n-float)))) is-not-odd)
      (when (and even (not (even? (int n-float)))) is-not-even)
      (when (and (present? in) (not (includes? in n-float))) is-not-in)
      (when (and (present? not-in) (includes? not-in n-float)) is-in))))

(defn length [record attr args]
  (let [length (str (count (attr record)))]
    (numericality {:len length} :len args)))

(defn inclusion [record attr {:keys [in]}]
  (let [attr-value (attr record)]
    (when-not (includes? in attr-value)
      "must be included in the list")))

(defn exclusion [record attr {:keys [from]}]
  (let [attr-value (attr record)]
    (when (includes? from attr-value)
      "is reserved")))

(defn formatted [record attr {:keys [pattern] :or {pattern #""}}]
  (let [attr-value (attr record)]
    (when-not (formatted? attr-value pattern)
      "has the incorrect format")))

(defn is-integer [record attr args]
  (let [attr-value (attr record)]
    (when-not (integer? attr-value)
      "must be an integer")))

(defn is-float [record attr args]
  (let [attr-value (attr record)]
    (when-not (float? attr-value)
      "must be a floating point number")))

; RFC 2822
(def email-pattern #"[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?")
(defn email [record attr {pattern :pattern}]
  (when (formatted record attr {:pattern email-pattern})
    "must be a valid email"))

(defn- special-validation [validatior-key]
  (case validatior-key
    :integer #'is-integer
    :float #'is-float
    nil))

(defn validation-factory [validatior-key]
  (if-let [fn (special-validation validatior-key)]
    fn
    (if-let [fn (ns-resolve 'metis.validations (symbol (name validatior-key)))]
      fn
      (throw (Exception. (str "Could not locate the validator: " (name validatior-key)))))))

