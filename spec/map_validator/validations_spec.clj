(ns map-validator.validations-spec
  (:use [speclj.core]
        [map-validator.validations]))

(describe "validations"

  (context "presence"
    (it "passes when the attribute is present"
      (should (:result (is-present? {:some-key "something"} :some-key {}))))

    (it "fails when attribute is nil"
      (should-not (:result (is-present? {:some-key nil} :some-key {}))))

    (it "fails when attribute is not present"
      (should-not (:result (is-present? {} :some-key {}))))

    (it "provides an error message upon failure"
      (should= default-presence-error-message (:error-message (is-present? {:key nil} :key {}))))

    )

  (context "format"
    (it "returns false if value is nil"
      (should-not (:result (is-formatted? {:key nil} :key {:pattern #""}))))

    (it "throws when pattern key of args map is not given"
      (should-throw Exception (is-formatted? nil nil {})))

    (it "provides an error message upon failure"
      (should= default-format-error-message (:error-message (is-formatted? {:key nil} :key {:pattern #""}))))

    )

  (context "email"
    (it "passes for valid email"
      (should (:result (is-email? {:email "snap.into@slim.jim"} :email {})))
      (should (:result (is-email? {:email "snapinto@slim.jim"} :email {})))

      )

    (it "fails for invalid email"
      (should-not (:result (is-email? {:email "snap@into@slim.jim"} :email {})))
      )

    (it "provides an error message upon failure"
      (should= default-email-error-message (:error-message (is-email? {:key nil} :key {}))))

    )

  (context "phone number"
    (it "passes for valid phone number"
      (should (:result (is-phone-number? {:number "800-800-1234"} :number {})))
      (should (:result (is-phone-number? {:number "800-800.1234"} :number {})))
      (should (:result (is-phone-number? {:number "800-800/1234"} :number {})))
      (should (:result (is-phone-number? {:number "800-800 1234"} :number {})))
      (should (:result (is-phone-number? {:number "800-800  1234"} :number {})))
      (should (:result (is-phone-number? {:number "800 800-1234"} :number {})))
      (should (:result (is-phone-number? {:number "800  800-1234"} :number {})))
      (should (:result (is-phone-number? {:number "800 800 1234"} :number {})))
      (should (:result (is-phone-number? {:number "800  800  1234"} :number {})))
      (should (:result (is-phone-number? {:number "  800  800  1234  "} :number {})))
      (should (:result (is-phone-number? {:number "8008001234"} :number {})))
      (should (:result (is-phone-number? {:number "01-800-1234"} :number {})))
      (should (:result (is-phone-number? {:number "(800)-800-1234"} :number {})))
      (should (:result (is-phone-number? {:number "(800) 800-1234"} :number {})))
      (should (:result (is-phone-number? {:number "(800)800-1234"} :number {})))
      (should (:result (is-phone-number? {:number "(800)8001234"} :number {})))
      (should (:result (is-phone-number? {:number "1800-800-1234"} :number {})))
      (should (:result (is-phone-number? {:number "200-800-800-1234"} :number {})))
      (should (:result (is-phone-number? {:number "200 800-800-1234"} :number {})))
      (should (:result (is-phone-number? {:number "200800-800-1234"} :number {})))
      (should (:result (is-phone-number? {:number "+200 800-800-1234"} :number {})))
      (should (:result (is-phone-number? {:number "  +  200 (800)-800-1234"} :number {}) ))
      )

    (it "fails for an invalid phone number"
      (should-not (:result (is-phone-number? {:number "2000-800-800-1234"} :number {})))
      (should-not (:result (is-phone-number? {:number "200-8000-800-1234"} :number {})))
      (should-not (:result (is-phone-number? {:number "200-800-8000-1234"} :number {})))
      (should-not (:result (is-phone-number? {:number "200-800-800-01234"} :number {})))
      (should-not (:result (is-phone-number? {:number "200-800-800-234"} :number {})))
      (should-not (:result (is-phone-number? {:number "200-800-80-1234"} :number {})))
      (should-not (:result (is-phone-number? {:number "200--800-1234"} :number {})))
      )

    (it "provides an error message upon failure"
      (should= default-phone-number-error-message (:error-message (is-phone-number? {:key nil} :key {}))))

    )

  (context "get validation"
    (it "takes a keyword and returns the built-in validator"
      (should= (ns-resolve 'map-validator.validations 'is-present?) (get-validation :is-present?)))

    (it "throws if the validation is not found"
      (should-throw Exception (get-validation :some-nonexistant-validation)))

    )

  )
