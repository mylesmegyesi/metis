(ns metis.validator.validations-spec
  (:use [speclj.core]
    [metis.validator.validations :rename {with my-with}]))

(describe "validations"

  (context "with"
    (it "returns nil if the validation passes"
      (should= nil (my-with nil {:validator (fn [attr] true)})))

    (it "returns an error message if the validation fails"
      (should-not= nil (my-with nil {:validator (fn [attr] false)})))

    (it "throws an exception if validator is not given"
      (let [message "some error message"]
        (should-throw Exception (my-with nil {}))))

    )

  (context "presence"
    (it "passes when the attribute is present"
      (should= nil (is-present "here!" {})))

    (it "fails when attribute is nil"
      (should-not= nil (is-present nil {})))

    (it "fails when attribute is an empty string"
      (should-not= nil (is-present "" {})))

    (it "fails when attribute is an empty collection"
      (should-not= nil (is-present [] {})))

    )

  (context "format"
    (it "failes if attr is nil"
      (should-not= nil (is-formatted nil {:pattern #""})))

    (it "throws when pattern key of args map is not given"
      (should-throw Exception (is-formatted nil {})))

    (it "returns nil when pattern matches"
      (should= nil (is-formatted "a" {:pattern #"a"})))

    )

  (context "email"
    (it "passes for valid email"
      (should= nil (is-email "snap.into@slim.jim" {}))
      (should= nil (is-email "snapinto@slim.jim" {})))

    (it "fails for invalid email"
      (should-not= nil (is-email "snap@into@slim.jim" {})))

    )

  (context "phone number"
    (it "passes for valid phone number"
      (should= nil (is-phone-number "800-800-1234" {}))
      (should= nil (is-phone-number "800-800.1234" {}))
      (should= nil (is-phone-number "800-800/1234" {}))
      (should= nil (is-phone-number "800-800 1234" {}))
      (should= nil (is-phone-number "800-800  1234" {}))
      (should= nil (is-phone-number "800 800-1234" {}))
      (should= nil (is-phone-number "800  800-1234" {}))
      (should= nil (is-phone-number "800 800 1234" {}))
      (should= nil (is-phone-number "800  800  1234" {}))
      (should= nil (is-phone-number "  800  800  1234  " {}))
      (should= nil (is-phone-number "8008001234" {}))
      (should= nil (is-phone-number "01-800-1234" {}))
      (should= nil (is-phone-number "(800)-800-1234" {}))
      (should= nil (is-phone-number "(800) 800-1234" {}))
      (should= nil (is-phone-number "(800)800-1234" {}))
      (should= nil (is-phone-number "(800)8001234" {}))
      (should= nil (is-phone-number "1800-800-1234" {}))
      (should= nil (is-phone-number "200-800-800-1234" {}))
      (should= nil (is-phone-number "200 800-800-1234" {}))
      (should= nil (is-phone-number "200800-800-1234" {}))
      (should= nil (is-phone-number "+200 800-800-1234" {}))
      (should= nil (is-phone-number "  +  200 (800)-800-1234" {}))
      )

    (it "fails for an invalid phone number"
      (should-not= nil (is-phone-number "2000-800-800-1234" {}))
      (should-not= nil (is-phone-number "200-8000-800-1234" {}))
      (should-not= nil (is-phone-number "200-800-8000-1234" {}))
      (should-not= nil (is-phone-number "200-800-800-01234" {}))
      (should-not= nil (is-phone-number "200-800-800-234" {}))
      (should-not= nil (is-phone-number "200-800-80-1234" {}))
      (should-not= nil (is-phone-number "200--800-1234" {}))
      )

    )

  (context "get validation"
    (it "takes a keyword and returns the built-in validator"
      (should= (ns-resolve 'metis.validator.validations 'is-present) (get-validation :is-present)))

    (it "throws if the validation is not found"
      (should-throw Exception (get-validation :some-nonexistant-validation)))

    )

  )
