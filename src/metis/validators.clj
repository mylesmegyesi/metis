(ns metis.validators
  (:use [metis.util]))

(defn with [record attr {validator :validator}]
  (if validator
    (when-not (validator record)
      "is invalid")
    (throw (Exception. "Validator not given."))))

(defn presence [record attr {}]
  (let [attr-value (get record attr)]
    (when-not (present? attr-value)
      "must be present")))

(defn acceptance [record attr {:keys [accept] :or {accept "1"}}]
  (let [attr-value (get record attr)]
    (when (not= attr-value accept)
      "must be accepted")))

(defn confirmation [record attr args]
  (let [{:keys [confirm] :or {confirm (keyword (str (keyword->str attr) "-confirmation"))}} args
        attr-value (get record attr)
        confirm-value (confirm record)]
    (when (not= attr-value confirm-value)
      "doesn't match confirmation")))

(def numericality-defaults {:only-integer false
                            :odd false
                            :even false
                            :is-not-an-int "must be an integer"
                            :is-not-a-number "must be a number"
                            :is-not-greater-than "must be greater than %s"
                            :is-not-greater-than-or-equal-to "must be greater than or equal to %s"
                            :is-not-equal-to "must be equal to %s"
                            :is-equal-to "must not be equal to %s"
                            :is-not-less-than "must be less than %s"
                            :is-not-less-than-or-equal-to "must be less than or equal to %s"
                            :is-not-odd "must be odd"
                            :is-not-even "must be even"
                            :is-not-in "must be included in the list"
                            :is-in "must not be included in the list"})

(defn numericality [record attr args]
  (let [attr-value (get record attr)
        {:keys [only-integer greater-than greater-than-or-equal-to equal-to not-equal-to less-than less-than-or-equal-to odd even in not-in is-not-an-int is-not-a-number is-not-greater-than is-not-greater-than-or-equal-to is-not-equal-to is-equal-to is-not-less-than is-not-less-than-or-equal-to is-not-odd is-not-even is-not-in is-in]} (merge numericality-defaults args)
        greater-than-f (when greater-than (float greater-than))
        greater-than-or-equal-to-f (when greater-than-or-equal-to (float greater-than-or-equal-to))
        equal-to-f (when equal-to (float equal-to))
        not-equal-to-f (when not-equal-to (float not-equal-to))
        less-than-f (when less-than (float less-than))
        less-than-or-equal-to-f (when less-than-or-equal-to (float less-than-or-equal-to))
        in (when in (map float in))
        not-in (when not-in (map float not-in))
        n-int (cond 
          (string? attr-value) (str->int attr-value)
          (integer? attr-value) attr-value 
          :else nil)
        n-float (cond
          (string? attr-value) (str->float attr-value)
          (integer? attr-value) (float attr-value)
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

(def length-defaults {:is-not-greater-than "must have length greater than %s"
                      :is-not-greater-than-or-equal-to "must have length greater than or equal to %s"
                      :is-not-equal-to "must have length equal to %s"
                      :is-equal-to "must have length not equal to %s"
                      :is-not-less-than "must have length less than %s"
                      :is-not-less-than-or-equal-to "must have length less than or equal to %s"
                      :is-not-odd "must have odd length"
                      :is-not-even "must be even length"
                      :is-not-in "must have length included in the list"
                      :is-in "must have lenght not included in the list"})

(defn length [record attr args]
  (let [length (str (count (get record attr)))]
    (numericality {:len length} :len (merge length-defaults args))))

(defn inclusion [record attr {:keys [in]}]
  (let [attr-value (get record attr)]
    (when-not (includes? in attr-value)
      "must be included in the list")))

(defn exclusion [record attr {:keys [from]}]
  (let [attr-value (get record attr)]
    (when (includes? from attr-value)
      "is reserved")))

(defn formatted [record attr {:keys [pattern] :or {pattern #""}}]
  (let [attr-value (get record attr)]
    (when-not (formatted? attr-value pattern)
      "has the incorrect format")))

(def email-pattern #"[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-zA-Z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?\.)+[a-zA-Z0-9](?:[a-zA-Z0-9-]*[a-zA-Z0-9])?")
(defn email [record attr {pattern :pattern}]
  (when (formatted record attr {:pattern email-pattern})
    "must be a valid email"))
