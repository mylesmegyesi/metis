(ns map-validator.validations
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
  (cond
    (string? attr) (if (blank? attr) message)
    (coll? attr) (if (empty? attr) message)
    :else (if (nil? attr) message)))

(defn- first-match [m]
  (if (coll? m) (first m) m))

(defn is-formatted [attr {:keys [pattern message] :or {message "has the incorrect format"}}]
  (if pattern
    (if-let [message (is-present attr {:message message})]
      message
      (let [match (first-match (re-matches pattern attr))]
        (when (or (nil? match) (not= match attr))
          message)))
    (throw (Exception. "Pattern to match with not given."))))

(defn is-email [email {:keys [message] :or {message "is not a valid email"}}]
  (is-formatted email {:message message :pattern email-pattern}))

(defn is-phone-number [phone-number {:keys [message] :or {message "is not a valid phone number"}}]
  (is-formatted phone-number {:message message :pattern phone-number-pattern}))

(defn get-validation [validatior-key]
  (if-let [fn (ns-resolve 'map-validator.validations (symbol (name validatior-key)))]
    fn
    (throw (Exception. (str "Could not locate the validator: " (name validatior-key))))))

