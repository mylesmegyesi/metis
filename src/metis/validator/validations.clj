(ns metis.validator.validations
  (:use [metis.validator.util]))

(defn with [record attr {validator :validator}]
  (if validator
    (when-not (validator record)
      "is invalid")
    (throw (Exception. "Validator not given."))))

(defn presence [record attr {}]
  (let [attr-value (attr record)]
    (when-not (present? attr-value)
      "is not present")))

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
        {:keys [only-integer greater-than greater-than-or-equal-to equal-to less-than less-than-or-equal-to odd even in]
         :or {only-integer false
              odd false
              even false}} args
        greater-than-f (when greater-than (float greater-than))
        greater-than-or-equal-to-f (when greater-than-or-equal-to (float greater-than-or-equal-to))
        equal-to-f (when equal-to (float equal-to))
        less-than-f (when less-than (float less-than))
        less-than-or-equal-to-f (when less-than-or-equal-to (float less-than-or-equal-to))
        in (when in (map float in))
        {:keys [not-an-int not-a-number not-greater-than not-greater-than-or-equal-to not-equal-to not-less-than not-less-than-or-equal-to not-odd not-even not-in]
         :or {not-an-int "is not an integer"
              not-a-number "is not a number"
              not-greater-than (str "is not greater than " greater-than)
              not-greater-than-or-equal-to (str "is not greater than or equal to " greater-than-or-equal-to)
              not-equal-to (str "is not equal to " equal-to)
              not-less-than (str "is not less than " less-than)
              not-less-than-or-equal-to (str "is not less than or equal to " less-than-or-equal-to)
              not-odd (str "is not odd")
              not-even (str "is not even")
              not-in (str "is not included in the list")}} args
        n-int
        (cond
          (string? attr-value) (str->int attr-value)
          (integer? attr-value) attr-value
          :else nil)
        n-float
        (cond
          (string? attr-value) (str->float attr-value)
          (integer? attr-value) (float attr-value)
          (float? attr-value) attr-value
          :else nil)]
    (or
      (when (and only-integer (not (integer? n-int))) not-an-int)
      (when-not (number? n-float) not-a-number)
      (when (and (present? greater-than-f) (not (> n-float greater-than-f))) not-greater-than)
      (when (and (present? greater-than-or-equal-to-f) (not (>= n-float greater-than-or-equal-to-f))) not-greater-than-or-equal-to)
      (when (and (present? equal-to-f) (not= n-float equal-to-f)) not-equal-to)
      (when (and (present? less-than-f) (not (< n-float less-than-f))) not-less-than)
      (when (and (present? less-than-or-equal-to-f) (not (<= n-float less-than-or-equal-to-f))) not-less-than-or-equal-to)
      (when (and odd (not (odd? (int n-float)))) not-odd)
      (when (and even (not (even? (int n-float)))) not-even)
      (when (and (present? in) (not (in? n-float in))) not-in))))

(defn length [record attr args]
  (let [len (count (attr record))]
    (numericality {:len len} :len (merge args {:only-integer true}))))

(defn inclusion [record attr {:keys [in] :or {in []}}]
  (let [attr-value (attr record)]
    (when-not (in? attr-value in)
      "is not included in the list")))

(defn exclusion [record attr {:keys [from] :or {from []}}]
  (let [attr-value (attr record)]
    (when (in? attr-value from)
      "is reserved")))

(defn formatted [record attr {:keys [pattern] :or {pattern #""}}]
  (let [attr-value (attr record)]
    (when-not (formatted? attr-value pattern)
      "has the incorrect format")))

(defn get-validation [validatior-key]
  (if-let [fn (ns-resolve 'metis.validator.validations (symbol (name validatior-key)))]
    fn
    (throw (Exception. (str "Could not locate the validator: " (name validatior-key))))))

