(ns map-validator.validations)

(def default-presence-error-message "is blank")
(defn is-present? [attrs attr args]
  (if (not (nil? (attr attrs)))
    {:result true}
    {:result false :error-message default-presence-error-message}
    ))

(defn- first-match [m]
  (if (coll? m) (first m) m))

(def default-format-error-message "has the incorrect format")
(defn is-formatted? [attrs attr args]
  (if-let [pattern (:pattern args)]
    (let [value (attr attrs)
          match (if (nil? value) value (first-match (re-matches pattern value)))]
      (if (and (not (nil? match)) (= match value))
        {:result true}
        {:result false :error-message default-format-error-message}))
    (throw (Exception. "Pattern to match with not given."))))

(defn- call-format [attrs attr args pattern default-message]
  (let [result (is-formatted? attrs attr (merge args {:pattern pattern}))]
    (if (:result result)
      result
      (merge result {:error-message default-message}))))

(def default-email-error-message "is not a valid email")
; RFC 2822
(def email-pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")
(defn is-email? [attrs attr args]
  (call-format attrs attr args email-pattern default-email-error-message))

(def default-phone-number-error-message "is not a valid phone number")
(def phone-number-pattern #"^(\s*)(\+?)(\s*[-|\.|\/]?\s*)(\d{0,3})(\s*[-|\.|\/]?\s*)(\d{1,3}|\((\d{1,3})\))(\s*[-|\.|\/]?\s*)(\d{3})(\s*[-|\.|\/]?\s*)(\d{4})(\s*)$")
(defn is-phone-number? [attrs attr args]
  (call-format attrs attr args phone-number-pattern default-phone-number-error-message))

(defn get-validation [validatior-key]
  (if-let [fn (ns-resolve 'map-validator.validations (symbol (name validatior-key)))]
    fn
    (throw (Exception. (str "Could not locate the validator: " (name validatior-key))))))
