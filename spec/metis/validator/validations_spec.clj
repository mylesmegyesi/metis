(ns metis.validator.validations-spec
  (:use [speclj.core]
    [metis.validator.validations :rename {with my-with}]))

(describe "validations"

  (context "with"
    (it "returns nil if the validation passes"
      (should= nil (my-with {} nil {:validator (fn [attrs] true)})))

    (it "returns an error message if the validation fails"
      (should-not= nil (my-with {} nil {:validator (fn [attrs] false)})))

    (it "throws an exception if validator is not given"
      (let [message "some error message"]
        (should-throw Exception (my-with nil {}))))

    )

  (context "presence"
    (it "passes when the attribute is present"
      (should= nil (presence {:foo "here!"} :foo {})))

    (it "fails when attribute is nil"
      (should-not= nil (presence {:foo nil} :foo {})))

    (it "fails when attribute is an empty string"
      (should-not= nil (presence {:foo ""} :foo {})))

    (it "fails when attribute is an empty collection"
      (should-not= nil (presence {:foo []} :foo {})))

    )

  (context "acceptance"
    (it "passes when accepted"
      (should= nil (acceptance {:foo "1"} :foo {})))

    (it "fails when not accepted"
      (should-not= nil (acceptance {:foo ""} :foo {})))

    (it "passes with customer accept"
      (should= nil (acceptance {:foo "yes"} :foo {:accept "yes"})))
    )

  (context "confirmation"
    (with-all email-to-validate "snap.into@slim.jim")
    (it "passes when confirmation is equal"
      (should= nil (confirmation {:email @email-to-validate :email-confirmation @email-to-validate} :email {})))

    (it "fails when confirmation is not equal"
      (should-not= nil (confirmation {:email @email-to-validate :email-confirmation "something else"} :email {})))

    (it "passes with custom confirmation attribute"
      (should= nil (confirmation {:email @email-to-validate :some-attr @email-to-validate} :email {:confirm :some-attr})))
    )

  (context "numericality"
    (it "passes when the attribute is an integer"
      (should= nil (numericality {:foo "1"} :foo {:only-integer true}))
      (should-not= nil (numericality {:foo "1.0"} :foo {:only-integer true})))

    (it "passes when the attribute is a number"
      (should= nil (numericality {:foo "1"} :foo {}))
      (should= nil (numericality {:foo "1.0"} :foo {})))

    (it "fails the the attribute is not a number"
      (should-not= nil (numericality {:foo "asdf"} :foo {})))

    (it "passes when the attribute is greater than"
      (should= nil (numericality {:foo "1"} :foo {:greater-than 0}))
      (should-not= nil (numericality {:foo "0"} :foo {:greater-than 0})))

    (it "passes when the attribute is greater than or equal to"
      (should= nil (numericality {:foo "1"} :foo {:greater-than-or-equal-to 1}))
      (should-not= nil (numericality {:foo "0"} :foo {:greater-than-or-equal-to 1})))

    (it "passes when the attribute is equal to"
      (should= nil (numericality {:foo "1"} :foo {:equal-to 1}))
      (should-not= nil (numericality {:foo "0"} :foo {:equal-to 1})))

    (it "passes when the attribute is less than"
      (should= nil (numericality {:foo "0"} :foo {:less-than 1}))
      (should-not= nil (numericality {:foo "1"} :foo {:less-than 1})))

    (it "passes when the attribute is less than or equal to"
      (should= nil (numericality {:foo "1"} :foo {:less-than-or-equal-to 1}))
      (should-not= nil (numericality {:foo "2"} :foo {:less-than-or-equal-to 1})))

    (it "passes when the attribute is odd"
      (should= nil (numericality {:foo "1"} :foo {:odd true}))
      (should-not= nil (numericality {:foo "2"} :foo {:odd true})))

    (it "passes when the attribute is even"
      (should= nil (numericality {:foo "2"} :foo {:even true}))
      (should-not= nil (numericality {:foo "1"} :foo {:even true})))

    (it "passes when the attribute's number is in the collection"
      (should-not= nil (numericality {:foo "7"} :foo {:in (range 5 7)}))
      (should= nil (numericality {:foo "6"} :foo {:in (range 5 7)}))
      (should= nil (numericality {:foo "5"} :foo {:in (range 5 7)}))
      (should-not= nil (numericality {:foo "4"} :foo {:in (range 5 7)})))

    )

  (context "length"
    (it "calls the numericality validation on the count of the attribute"
      (should= nil (length {:foo "1234"} :foo {:equal-to 4})))

    )

  (context "inclusion"
    (it "passes when the item is in the collection"
      (should= nil (inclusion {:foo "1"} :foo {:in ["1" "2" "3" "4"]})))

    (it "passes when the item is in the collection"
      (should-not= nil (inclusion {:foo "5"} :foo {:in ["1" "2" "3" "4"]})))

    )

  (context "exclusion"
    (it "passes when the item is in the collection"
      (should= nil (exclusion {:foo "5"} :foo {:from ["1" "2" "3" "4"]})))

    (it "passes when the item is in the collection"
      (should-not= nil (exclusion {:foo "1"} :foo {:from ["1" "2" "3" "4"]})))

    )

  (context "formatted"
    (it "failes if attr is nil"
      (should-not= nil (formatted {:foo nil} :foo {:pattern #""})))

    (it "returns nil when pattern matches"
      (should= nil (formatted {:foo "a"} :foo {:pattern #"a"})))

    )

  (context "email"
    (it "passes for valid email"
      (should= nil (email {:foo "snap.into@slim.jim"} :foo {}))
      (should= nil (email {:foo "snapinto@slim.jim"} :foo {})))

    (it "fails for invalid email"
      (should-not= nil (email {:foo "snap@into@slim.jim"} :foo {})))

    )

  (context "phone number"
    (it "passes for valid phone number"
      (should= nil (phone-number {:foo "800-800-1234"} :foo {}))
      (should= nil (phone-number {:foo "800-800.1234"} :foo {}))
      (should= nil (phone-number {:foo "800-800/1234"} :foo {}))
      (should= nil (phone-number {:foo "800-800 1234"} :foo {}))
      (should= nil (phone-number {:foo "800-800  1234"} :foo {}))
      (should= nil (phone-number {:foo "800 800-1234"} :foo {}))
      (should= nil (phone-number {:foo "800  800-1234"} :foo {}))
      (should= nil (phone-number {:foo "800 800 1234"} :foo {}))
      (should= nil (phone-number {:foo "800  800  1234"} :foo {}))
      (should= nil (phone-number {:foo "  800  800  1234  "} :foo {}))
      (should= nil (phone-number {:foo "8008001234"} :foo {}))
      (should= nil (phone-number {:foo "01-800-1234"} :foo {}))
      (should= nil (phone-number {:foo "(800)-800-1234"} :foo {}))
      (should= nil (phone-number {:foo "(800) 800-1234"} :foo {}))
      (should= nil (phone-number {:foo "(800)800-1234"} :foo {}))
      (should= nil (phone-number {:foo "(800)8001234"} :foo {}))
      (should= nil (phone-number {:foo "1800-800-1234"} :foo {}))
      (should= nil (phone-number {:foo "200-800-800-1234"} :foo {}))
      (should= nil (phone-number {:foo "200 800-800-1234"} :foo {}))
      (should= nil (phone-number {:foo "200800-800-1234"} :foo {}))
      (should= nil (phone-number {:foo "+200 800-800-1234"} :foo {}))
      (should= nil (phone-number {:foo "  +  200 (800)-800-1234"} :foo {}))
      )

    (it "fails for an invalid phone number"
      (should-not= nil (phone-number {:foo "2000-800-800-1234"} :foo {}))
      (should-not= nil (phone-number {:foo "200-8000-800-1234"} :foo {}))
      (should-not= nil (phone-number {:foo "200-800-8000-1234"} :foo {}))
      (should-not= nil (phone-number {:foo "200-800-800-01234"} :foo {}))
      (should-not= nil (phone-number {:foo "200-800-800-234"} :foo {}))
      (should-not= nil (phone-number {:foo "200-800-80-1234"} :foo {}))
      (should-not= nil (phone-number {:foo "200--800-1234"} :foo {}))
      )

    )

  (context "get validation"
    (it "takes a keyword and returns the built-in validator"
      (should= (ns-resolve 'metis.validator.validations 'presence) (get-validation :presence)))

    (it "throws if the validation is not found"
      (should-throw Exception (get-validation :some-nonexistant-validation)))

    )

  )
