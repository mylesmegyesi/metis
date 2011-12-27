(ns validator.validations
  (:use [clojure.string :only [blank?]]))

; RFC 2822
(def email-pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")
(def phone-number-pattern #"^(\s*)(\+?)(\s*[-|\.|\/]?\s*)(\d{0,3})(\s*[-|\.|\/]?\s*)(\d{1,3}|\((\d{1,3})\))(\s*[-|\.|\/]?\s*)(\d{3})(\s*[-|\.|\/]?\s*)(\d{4})(\s*)$")

(defn with [attr {:keys [validator message] :or {message "is invalid"}}]
  (if validator
    (when-not (validator attr)
      message)
    (throw (Exception. "Validator not given."))))

(defn is-present [attr {:keys [message] :or {message "is not present"}}]
  (with attr {:message message :validator (fn [attr]
    (not (cond
      (string? attr) (blank? attr)
      (coll? attr) (empty? attr)
      :else (nil? attr))))}))

(defn- first-match [m]
  (if (coll? m) (first m) m))

(defn is-formatted [attr {:keys [pattern message] :or {message "has the incorrect format"}}]
  (with attr {:message message :validator (fn [attr]
    (when (nil? pattern)
      (throw (Exception. "Pattern to match with not given.")))
    (when (not (nil? attr))
      (let [match (first-match (re-matches pattern attr))]
        (= match attr))))}))

(defn is-email [email {:keys [message] :or {message "is not a valid email"}}]
  (is-formatted email {:message message :pattern email-pattern}))

(defn is-phone-number [phone-number {:keys [message] :or {message "is not a valid phone number"}}]
  (is-formatted phone-number {:message message :pattern phone-number-pattern}))

(defn get-validation [validatior-key]
  (if-let [fn (ns-resolve 'validator.validations (symbol (name validatior-key)))]
    fn
    (throw (Exception. (str "Could not locate the validator: " (name validatior-key))))))

