(ns map-validator.validations)

(defn- first-match [m]
  (if (coll? m) (first m) m))

(defn is-present? [attrs attr]
  (not (nil? (attr attrs))))

(defn format [attrs attr pattern]
  (let [value (attr attrs)
        match (first-match (re-matches pattern value))]
    (if (and (not (nil? match)) (= match value))
      {:result true}
      {:result false})))

; RFC 2822
(def email-pattern #"[a-z0-9!#$%&'*+/=?^_`{|}~-]+(?:\.[a-z0-9!#$%&'*+/=?^_`{|}~-]+)*@(?:[a-z0-9](?:[a-z0-9-]*[a-z0-9])?\.)+[a-z0-9](?:[a-z0-9-]*[a-z0-9])?")
(defn is-email? [attrs attr]
  (format attrs attr email-pattern))

(def phone-number-pattern #"^(\s*)(\+?)(\s*[-|\.|\/]?\s*)(\d{0,3})(\s*[-|\.|\/]?\s*)(\d{1,3}|\((\d{1,3})\))(\s*[-|\.|\/]?\s*)(\d{3})(\s*[-|\.|\/]?\s*)(\d{4})(\s*)$")
(defn is-phone-number? [attrs attr]
  (format attrs attr phone-number-pattern))
