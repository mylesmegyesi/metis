(ns metis.validator.validations
  (:use [clojure.string :only [blank?]]))

; RFC 2822
(def email-pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")
(def phone-number-pattern #"^(\s*)(\+?)(\s*[-|\.|\/]?\s*)(\d{0,3})(\s*[-|\.|\/]?\s*)(\d{1,3}|\((\d{1,3})\))(\s*[-|\.|\/]?\s*)(\d{3})(\s*[-|\.|\/]?\s*)(\d{4})(\s*)$")

(defn with [attr {validator :validator}]
  (if validator
    (when-not (validator attr)
      "is invalid")
    (throw (Exception. "Validator not given."))))

(defn- present? [attr]
  (not (cond
    (string? attr) (blank? attr)
    (coll? attr) (empty? attr)
    :else (nil? attr))))

(defn is-present [attr {}]
  (when-not (present? attr)
    "is not present"))

(defn- first-match [m]
  (if (coll? m) (first m) m))

(defn- formatted? [attr pattern]
  (when (nil? pattern)
    (throw (Exception. "Pattern to match with not given.")))
  (when (not (nil? attr))
    (let [match (first-match (re-matches pattern attr))]
      (= match attr))))

(defn is-formatted [attr {pattern :pattern}]
  (when-not (formatted? attr pattern)
    "has the incorrect format"))

(defn is-email [email {pattern :pattern}]
  (when (is-formatted email {:pattern email-pattern})
    "is not a valid email"))

(defn is-phone-number [phone-number {pattern :pattern}]
  (when (is-formatted phone-number {:pattern phone-number-pattern})
    "is not a valid phone number"))

(defn get-validation [validatior-key]
  (if-let [fn (ns-resolve 'metis.validator.validations (symbol (name validatior-key)))]
    fn
    (throw (Exception. (str "Could not locate the validator: " (name validatior-key))))))

