(ns map-validator.validations-spec
  (:use [speclj.core]
        [map-validator.validations]))

(describe "validations"

  (context "presence"
    (it "passes when the attribute is present"
      (should (is-present? {:some-key "something"} :some-key)))

    (it "fails when attribute is nil"
      (should-not (is-present? {:some-key nil} :some-key)))

    (it "fails when attribute is not present"
      (should-not (is-present? {} :some-key)))
    )

  (context "email"
    (it "passes for valid email"
      (should (:result (is-email? {:email "snap.into@slim.jim"} :email)))
      (should (:result (is-email? {:email "snapinto@slim.jim"} :email)))

      )

    (it "fails for invalid email"
      (should-not (:result (is-email? {:email "snap@into@slim.jim"} :email)))
      )

    )

  (context "phone number"
    (it "passes for valid phone number"
      (should (:result (is-phone-number? {:number "800-800-1234"} :number)))
      (should (:result (is-phone-number? {:number "800-800.1234"} :number)))
      (should (:result (is-phone-number? {:number "800-800/1234"} :number)))
      (should (:result (is-phone-number? {:number "800-800 1234"} :number)))
      (should (:result (is-phone-number? {:number "800-800  1234"} :number)))
      (should (:result (is-phone-number? {:number "800 800-1234"} :number)))
      (should (:result (is-phone-number? {:number "800  800-1234"} :number)))
      (should (:result (is-phone-number? {:number "800 800 1234"} :number)))
      (should (:result (is-phone-number? {:number "800  800  1234"} :number)))
      (should (:result (is-phone-number? {:number "  800  800  1234  "} :number)))
      (should (:result (is-phone-number? {:number "8008001234"} :number)))
      (should (:result (is-phone-number? {:number "01-800-1234"} :number)))
      (should (:result (is-phone-number? {:number "(800)-800-1234"} :number)))
      (should (:result (is-phone-number? {:number "(800) 800-1234"} :number)))
      (should (:result (is-phone-number? {:number "(800)800-1234"} :number)))
      (should (:result (is-phone-number? {:number "(800)8001234"} :number)))
      (should (:result (is-phone-number? {:number "1800-800-1234"} :number)))
      (should (:result (is-phone-number? {:number "200-800-800-1234"} :number)))
      (should (:result (is-phone-number? {:number "200 800-800-1234"} :number)))
      (should (:result (is-phone-number? {:number "200800-800-1234"} :number)))
      (should (:result (is-phone-number? {:number "+200 800-800-1234"} :number)))
      (should (:result (is-phone-number? {:number "  +  200 (800)-800-1234"} :number)))
      )

    (it "fails for an invalid phone number"
      (should-not (:result (is-phone-number? {:number "2000-800-800-1234"} :number)))
      (should-not (:result (is-phone-number? {:number "200-8000-800-1234"} :number)))
      (should-not (:result (is-phone-number? {:number "200-800-8000-1234"} :number)))
      (should-not (:result (is-phone-number? {:number "200-800-800-01234"} :number)))
      (should-not (:result (is-phone-number? {:number "200-800-800-234"} :number)))
      (should-not (:result (is-phone-number? {:number "200-800-80-1234"} :number)))
      (should-not (:result (is-phone-number? {:number "200--800-1234"} :number)))
      )

    )

  )
