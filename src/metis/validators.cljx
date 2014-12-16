(ns metis.validators
  (:require [metis.util :as util]
            #+cljs [goog.string.format])
  #+clj (:import [org.apache.commons.validator.routines EmailValidator UrlValidator]))

(defn with [map _ {validator :validator}]
  (if validator
    (when-not (validator map)
      "is invalid")
    (throw (#+clj Exception. #+cljs js/Error. "Validator not given."))))

(defn contains [map key _]
  (when-not (contains? map key)
    "must contain key"))

(defn presence [map key _]
  (when-not (util/present? (get map key))
    "must be present"))

#+clj
(defn satisfies-protocol [map key {:keys [protocol]}]
  (when-not (satisfies? protocol (get map key))
    (str "must satisfy protocol " (:var protocol))))

(defn acceptance [map key {:keys [accept] :or {accept "1"}}]
  (when (not= accept (get map key))
    "must be accepted"))

(defn confirmation [map key {:keys [confirm] :or {confirm (keyword (str (name key) "-confirmation"))}}]
  (when (not= (get map key) (get map confirm))
    "doesn't match confirmation"))

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

(defn- format-str [f s]
  #+clj (format f s)
  #+cljs (goog.string.format f s))

(defn numericality [values key args]
  (let [value (get values key)
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
          (string? value) (util/str->int value)
          (integer? value) value
          :else nil)
        n-float (cond
          (string? value) (util/str->float value)
          (integer? value) (float value)
          (number? value) value
          :else nil)]
    (or
      (when (and only-integer (not (integer? n-int))) is-not-an-int)
      (when-not (number? n-float) is-not-a-number)
      (when (and (util/present? greater-than-f) (not (> n-float greater-than-f))) (format-str is-not-greater-than greater-than))
      (when (and (util/present? greater-than-or-equal-to-f) (not (>= n-float greater-than-or-equal-to-f))) (format-str is-not-greater-than-or-equal-to greater-than-or-equal-to))
      (when (and (util/present? equal-to-f) (not= n-float equal-to-f)) (format-str is-not-equal-to equal-to))
      (when (and (util/present? not-equal-to-f) (= n-float not-equal-to-f)) (format-str is-equal-to not-equal-to))
      (when (and (util/present? less-than-f) (not (< n-float less-than-f))) (format-str is-not-less-than less-than))
      (when (and (util/present? less-than-or-equal-to-f) (not (<= n-float less-than-or-equal-to-f))) (format-str is-not-less-than-or-equal-to less-than-or-equal-to))
      (when (and odd (not (odd? (int n-float)))) is-not-odd)
      (when (and even (not (even? (int n-float)))) is-not-even)
      (when (and (util/present? in) (not (util/includes? in n-float))) is-not-in)
      (when (and (util/present? not-in) (util/includes? not-in n-float)) is-in))))

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

(defn length [map key args]
  (let [length (str (count (get map key)))]
    (numericality {:len length} :len (merge length-defaults args))))

(defn inclusion [map key {:keys [in]}]
  (when-not (util/includes? in (get map key))
    "must be included in the list"))

(defn exclusion [record key {:keys [from]}]
  (let [key-value (get record key)]
    (when (util/includes? from key-value)
      "is reserved")))

(defn formatted [map key {:keys [pattern] :or {pattern #""}}]
  (when-not (util/formatted? (get map key) pattern)
    "has the incorrect format"))

#+clj
(def email-validator (EmailValidator/getInstance))

#+clj
(defn email [map key _]
  (when-not (.isValid email-validator (get map key))
    "must be a valid email"))

#+clj
(def url-option-values
  {:allow-two-slashes UrlValidator/ALLOW_2_SLASHES
   :allow-all-schemes UrlValidator/ALLOW_ALL_SCHEMES
   :no-fragments UrlValidator/NO_FRAGMENTS
   :allow-local-urls UrlValidator/ALLOW_LOCAL_URLS})

#+clj
(defn- url-option-value [options]
  (reduce
    (fn [opts option]
      (if (option options)
        (+ opts (option url-option-values))
        opts))
    0
    (keys url-option-values)))

#+clj
(defn- url-validator [{:keys [schemes] :as options}]
  (let [option-value (url-option-value options)
        option-value-zero? (zero? option-value)]
    (cond
      (and schemes (not option-value-zero?))
      (UrlValidator. (into-array schemes) option-value)
      (and schemes option-value-zero?)
      (UrlValidator. (into-array schemes))
      (and (not schemes) (not option-value-zero?))
      (UrlValidator. option-value)
      :else
      (UrlValidator.))))

#+clj
(defn url [map key options]
  (when-not (.isValid (url-validator options) (get map key))
    "must be a valid url"))

