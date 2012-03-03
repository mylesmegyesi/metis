(ns metis.validations-spec
  (:use [speclj.core]
    [metis.validations :rename {with my-with}]))

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
    (it "fails if it isn't supplied with a string"
      (should-not= nil (numericality {:foo 1} :foo {})))

    (it "passes when the attribute is a number"
      (should= nil (numericality {:foo "1"} :foo {}))
      (should= nil (numericality {:foo "1.0"} :foo {})))

    (it "fails the the attribute is not a number"
      (should-not= nil (numericality {:foo "asdf"} :foo {}))
      (should= "some message" (numericality {:foo "asdf"} :foo {:is-not-a-number "some message"})))
    
    (it "only-integer"
      (should= nil (numericality {:foo "1"} :foo {:only-integer true}))
      (should-not= nil (numericality {:foo "1.0"} :foo {:only-integer true}))
      (should= "some message" (numericality {:foo "1.0"} :foo {:only-integer true :is-not-an-int "some message"})))

    (it "greater-than"
      (should= nil (numericality {:foo "1"} :foo {:greater-than 0}))
      (should-not= nil (numericality {:foo "0"} :foo {:greater-than 0}))
      (should= "some 0" (numericality {:foo "0"} :foo {:greater-than 0 :is-not-greater-than "some %d"})))

    (it "greater-than-or-equal-to"
      (should= nil (numericality {:foo "1"} :foo {:greater-than-or-equal-to 1}))
      (should-not= nil (numericality {:foo "0"} :foo {:greater-than-or-equal-to 1}))
      (should= "some 1" (numericality {:foo "0"} :foo {:greater-than-or-equal-to 1 :is-not-greater-than-or-equal-to "some %s"})))

    (it "equal-to"
      (should= nil (numericality {:foo "1"} :foo {:equal-to 1}))
      (should-not= nil (numericality {:foo "0"} :foo {:equal-to 1}))
      (should= "some 1" (numericality {:foo "0"} :foo {:equal-to 1 :is-not-equal-to "some %d"})))

    (it "not-equal-to"
      (should= nil (numericality {:foo "1"} :foo {:not-equal-to 2}))
      (should-not= nil (numericality {:foo "0"} :foo {:not-equal-to 0}))
      (should= "some 0" (numericality {:foo "0"} :foo {:not-equal-to 0 :is-equal-to "some %d"})))

    (it "less-than"
      (should= nil (numericality {:foo "0"} :foo {:less-than 1}))
      (should-not= nil (numericality {:foo "1"} :foo {:less-than 1}))
      (should= "some 1" (numericality {:foo "1"} :foo {:less-than 1 :is-not-less-than "some %d"})))

    (it "less-than-or-equal-to"
      (should= nil (numericality {:foo "1"} :foo {:less-than-or-equal-to 1}))
      (should-not= nil (numericality {:foo "2"} :foo {:less-than-or-equal-to 1}))
      (should= "some 1" (numericality {:foo "2"} :foo {:less-than-or-equal-to 1 :is-not-less-than-or-equal-to "some %d"})))

    (it "odd"
      (should= nil (numericality {:foo "1"} :foo {:odd true}))
      (should-not= nil (numericality {:foo "2"} :foo {:odd true}))
      (should= "some" (numericality {:foo "2"} :foo {:odd true :is-not-odd "some"})))

    (it "even"
      (should= nil (numericality {:foo "2"} :foo {:even true}))
      (should-not= nil (numericality {:foo "1"} :foo {:even true}))
      (should= "some" (numericality {:foo "1"} :foo {:even true :is-not-even "some"})))

    (it "in"
      (should-not= nil (numericality {:foo "7"} :foo {:in (range 5 7)}))
      (should= nil (numericality {:foo "6"} :foo {:in (range 5 7)}))
      (should= nil (numericality {:foo "5"} :foo {:in (range 5 7)}))
      (should-not= nil (numericality {:foo "4"} :foo {:in (range 5 7)}))
      (should= "some" (numericality {:foo "4"} :foo {:in (range 5 7) :is-not-in "some"})))

    (it "not in"
      (should= nil (numericality {:foo "4"} :foo {:not-in (range 5 7)}))
      (should= nil (numericality {:foo "8"} :foo {:not-in (range 5 7)}))
      (should-not= nil (numericality {:foo "5"} :foo {:not-in (range 5 7)}))
      (should= "some" (numericality {:foo "5"} :foo {:not-in (range 5 7) :is-in "some"})))
    
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
    (it "fails if attr is nil"
      (should-not= nil (formatted {:foo nil} :foo {:pattern #""})))

    (it "returns nil when pattern matches"
      (should= nil (formatted {:foo "a"} :foo {:pattern #"a"})))

    )

  (context "is-integer"
    (it "passes if the given value is an integer"
      (should= nil (is-integer {:foo 1} :foo {})))

    (it "fails if the given value isn't an integer"
      (should-not= nil (is-integer {:foo "1"} :foo {}))
      (should-not= nil (is-integer {:foo 1.0} :foo {})))

    )

  (context "is-float"
    (it "passes if the given value is an float"
      (should= nil (is-float {:foo 1.0} :foo {})))

    (it "fails if the given value isn't an integer"
      (should-not= nil (is-float {:foo "1"} :foo {}))
      (should-not= nil (is-float {:foo 1} :foo {})))

    )

  (context "email"
    (it "passes for valid email"
      (should= nil (email {:foo "snap.into@slim.jim"} :foo {}))
      (should= nil (email {:foo "s.n.a.p.i.n.t.o@s.l.i.m.j.i.com"} :foo {}))
      (should= nil (email {:foo "come@me.bro"} :foo {}))
      (should= nil (email {:foo "COME@me.bro"} :foo {}))
      (should= nil (email {:foo "COME.ME@ME.bro"} :foo {}))
      (should= nil (email {:foo "COME.ME@ME.BRO"} :foo {})))

    (it "fails for invalid email"
      (should-not= nil (email {:foo "snap@into@slim.jim"} :foo {})))

    )

  (context "get validation"
    (it "takes a keyword and returns the built-in validator"
      (should= (ns-resolve 'metis.validations 'with) (validation-factory :with))
      (should= (ns-resolve 'metis.validations 'presence) (validation-factory :presence))
      (should= (ns-resolve 'metis.validations 'acceptance) (validation-factory :acceptance))
      (should= (ns-resolve 'metis.validations 'confirmation) (validation-factory :confirmation))
      (should= (ns-resolve 'metis.validations 'numericality) (validation-factory :numericality))
      (should= (ns-resolve 'metis.validations 'length) (validation-factory :length))
      (should= (ns-resolve 'metis.validations 'inclusion) (validation-factory :inclusion))
      (should= (ns-resolve 'metis.validations 'exclusion) (validation-factory :exclusion))
      (should= (ns-resolve 'metis.validations 'formatted) (validation-factory :formatted))
      (should= (ns-resolve 'metis.validations 'is-integer) (validation-factory :integer))
      (should= (ns-resolve 'metis.validations 'is-float) (validation-factory :float))
      (should= (ns-resolve 'metis.validations 'email) (validation-factory :email)))

    (it "throws if the validation is not found"
      (should-throw Exception (validation-factory :some-nonexistant-validation)))

    )

  )
