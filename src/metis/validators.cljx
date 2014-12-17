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

(def numericality-defaults {:only-integer                    false
                            :odd                             false
                            :even                            false
                            :is-not-an-int                   "must be an integer"
                            :is-not-a-number                 "must be a number"
                            :is-not-greater-than             "must be greater than %s"
                            :is-not-greater-than-or-equal-to "must be greater than or equal to %s"
                            :is-not-equal-to                 "must be equal to %s"
                            :is-equal-to                     "must not be equal to %s"
                            :is-not-less-than                "must be less than %s"
                            :is-not-less-than-or-equal-to    "must be less than or equal to %s"
                            :is-not-odd                      "must be odd"
                            :is-not-even                     "must be even"
                            :is-not-in                       "must be included in the list"
                            :is-in                           "must not be included in the list"})
(defn- format-str [f s]
  #+clj (format f s)
  #+cljs (goog.string.format f s))

(defn ->number [value]
  #+clj
  (cond
    (string? value) (or
                      (try (Long/parseLong value) (catch Exception _ nil))
                      (try (Double/parseDouble value) (catch Exception _ nil)))
    (number? value) value
    :default nil)
  #+cljs
  (let [num (js/Number value)]
    (when-not (js/isNaN num)
      num)))

(defn- ->integer [value]
  #+clj
  (cond
    (string? value) (or
                      (try (Long/parseLong value) (catch Exception _ nil))
                      (int (try (Double/parseDouble value) (catch Exception _ nil))))
    (number? value) (int value)
    :default nil)
  #+cljs
  (let [number (js/Number value)]
    (when-not (js/isNaN number)
      (int number))))

(defn- any-number [value options]
  (when-not (->number value)
    (:is-not-a-number options)))

(defn- only-integer [value options]
  (when (:only-integer options)
    (when-not (integer? (->number value))
      (:is-not-an-int options))))

(defn- greater-than [value options]
  (when-let [other (:greater-than options)]
    (when-not (> (->number value) other)
      (format-str (:is-not-greater-than options) other))))

(defn- greater-than-or-equal-to [value options]
  (when-let [other (:greater-than-or-equal-to options)]
    (when-not (>= (->number value) other)
      (format-str (:is-not-greater-than-or-equal-to options) other))))

(defn- less-than [value options]
  (when-let [other (:less-than options)]
    (when-not (< (->number value) other)
      (format-str (:is-not-less-than options) other))))

(defn- less-than-or-equal-to [value options]
  (when-let [other (:less-than-or-equal-to options)]
    (when-not (<= (->number value) other)
      (format-str (:is-not-less-than-or-equal-to options) other))))

(defn- equal-to [value options]
  (when-let [other (:equal-to options)]
    (when-let [number (->number value)]
      (when-not (if (integer? number)
                  (= number other)
                  (= number (double other)))
        (format-str (:is-not-equal-to options) other)))))

(defn- not-equal-to [value options]
  (when-let [other (:not-equal-to options)]
    (when-let [number (->number value)]
      (when-not (if (integer? number)
                  (not= number other)
                  (not= number (double other)))
        (format-str (:is-equal-to options) other)))))

(defn- even [value options]
  (when (:even options)
    (when-let [number (->integer value)]
      (when-not (even? number)
        (:is-not-even options)))))

(defn- odd [value options]
  (when (:odd options)
    (when-let [number (->integer value)]
      (when-not (odd? number)
        (:is-not-odd options)))))

(defn- in [value options]
  (when-let [range (:in options)]
    (when-let [number (->number value)]
      (when-not ((set (if (integer? number) range (map double range))) (->number value))
        (:is-not-in options)))))

(defn- not-in [value options]
  (when-let [range (:not-in options)]
    (when-let [number (->number value)]
      (when ((set (if (integer? number) range (map double range))) (->number value))
        (:is-in options)))))

(def numericality-checks
  [only-integer
   greater-than
   greater-than-or-equal-to
   less-than
   less-than-or-equal-to
   equal-to
   not-equal-to
   even
   odd
   in
   not-in
   any-number])

(defn numericality [entity key options]
  (let [value (get entity key)
        options (merge numericality-defaults options)]
    (first
      (filter identity
        (map #(% value options) numericality-checks)))))

(def length-defaults {:is-not-greater-than             "must have length greater than %s"
                      :is-not-greater-than-or-equal-to "must have length greater than or equal to %s"
                      :is-not-equal-to                 "must have length equal to %s"
                      :is-equal-to                     "must have length not equal to %s"
                      :is-not-less-than                "must have length less than %s"
                      :is-not-less-than-or-equal-to    "must have length less than or equal to %s"
                      :is-not-odd                      "must have odd length"
                      :is-not-even                     "must be even length"
                      :is-not-in                       "must have length included in the list"
                      :is-in                           "must have lenght not included in the list"})

(defn length [map key args]
  (let [length (str (count (get map key)))]
    (numericality {:len length} :len (merge length-defaults args))))

(defn inclusion [map key {:keys [in]}]
  (when-not ((set in) (get map key))
    "must be included in the list"))

(defn exclusion [record key {:keys [from]}]
  (let [key-value (get record key)]
    (when ((set from) key-value)
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
   :no-fragments      UrlValidator/NO_FRAGMENTS
   :allow-local-urls  UrlValidator/ALLOW_LOCAL_URLS})

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

