(ns metis.validator.core-spec
  (:use [speclj.core]
    [clojure.repl :only [source]]
    [metis.validator.core]
    [metis.validator.validations :only [presence with] :rename {with my-with}]))

(defvalidator generic-record-validator
  (validate [:first-name :zipcode] [:presence {:allow-blank true}]))

(defvalidator GenericRecordValidator
  (validate [:first-name :zipcode] [:presence {:allow-blank true}]))

(describe "validator"

  (context "validate attr"
    (it "runs the validation"
      (should-not= nil (validate-attr {:foo nil} :foo :presence {}))
      (should= nil (validate-attr {:foo "something"} :foo :presence {}))
      (should= "message" (validate-attr {:foo ""} :foo :presence {:message "message"})))

    (it "uses the given error message"
      (let [message "other message"]
        (should= message (validate-attr {:foo nil} :foo :presence {:message message}))))

    (it "allows nil"
      (let [message "other message"]
        (should= nil (validate-attr {:foo nil} :foo :presence {:message message :allow-nil true}))))

    (it "allows blank"
      (let [message "other message"]
        (should= nil (validate-attr {:foo ""} :foo :presence {:message message :allow-blank true}))))

    )

  (context "normalize keywords"
    (it "given one keyword it returns a collection"
      (should= [:foo] (normalize-attributes :foo)))

    (it "given one keyword in a collection it returns a collection"
      (should= [:foo] (normalize-attributes [:foo])))

    (it "given multiple keywords in a collection it returns a collection"
      (should= [:foo :bar] (normalize-attributes [:foo :bar])))

    (it "given one validation without args it returns a collection with args"
      (should= [[:foo {}]] (normalize-validations :foo)))

    (it "given one validation without args it returns a collection with args"
      (should= [[:foo {}]] (normalize-validations [:foo])))

    (it "given one keyword in a collection it returns a collection"
      (should= [[:foo {}]] (normalize-validations [:foo {}])))

    (it "given multiple validations in a collection it returns a collection of validations with args"
      (should= [[:foo {}] [:bar {}]] (normalize-validations [:foo :bar])))

    (it "given multiple validations in a collection it returns a collection of validations with args"
      (should= [[:foo {:thing "here"}] [:bar {}] [:baz {:one "two"}]] (normalize-validations [:foo {:thing "here"} :bar :baz {:one "two"}])))
    )

  (context "validate"
    (it "accepts an attribute to validate and a validation as keywords"
      (let [errors (validate {:foo "foo"} :foo :presence)]
        (should= {} errors)))

    (it "accepts an attribute to validate as a collection"
      (let [errors (validate {:foo "foo"} [:foo] :presence)]
        (should= {} errors)))

    (it "accepts many attributes to validate as a collection"
      (let [errors (validate {:foo "foo"} [:foo :bar] :presence)]
        (should (:bar errors))))

    (it "accepts validations as a keyword with arguements"
      (let [errors (validate {:foo "foo"} :foo :presence {})]
        (should= {} errors)))

    (it "accepts a validation as a collection without arguements"
      (let [errors (validate {:foo "foo"} :foo [:presence])]
        (should= {} errors)))

    (it "accepts validations as a collection with arguements"
      (let [errors (validate {:foo "foo"} :foo [:presence {}])]
        (should= {} errors)))

    (it "accepts multiple validations with or without arguements in a collection"
      (let [message "some error"
            errors (validate {:foo "foo"} :foo [:presence :length {:equal-to 3} :email {:message message}])]
        (should= {:foo [message]} errors)))

    (it "returns a map with a collection of errors"
      (let [message "error"
            errors (validate {:foo ""} :foo [:presence {:message message}])]
        (should= {:foo [message]} errors)))

    (it "runs all validations and returns errors"
      (let [message "error"
            errors (validate {:foo ""} :foo [:presence {:message message} :with {:validator (fn [_] true) :message message}])]
        (should= {:foo [message]} errors)))

    )

  (context "merge errors"
    (it "combines to maps of errors into one"
      (should= {:foo ["bar" "baz"]} (merge-errors {:foo ["bar"]} {:foo ["baz"]}))
      (should= {:foo ["bar" "baz"] :baz ["bar"]} (merge-errors {:foo ["bar"]} {:foo ["baz"]} {:baz ["bar"]})))

    )

  (context "remove empty errors"
    (it "removes keys that have no errors"
      (should= {} (remove-empty-errors {:first-name []})))
    (it "keeps keys that have errors"
      (should= {:first-name ["invalid"]} (remove-empty-errors {:first-name ["invalid"]})))

    )

  (context "defvalidator"
    (it "defines a validator"
      (should= {} (generic-record-validator {:first-name "Guy" :zipcode ""}))
      (should (:first-name (generic-record-validator {:first-name nil :zipcode "12345"})))
      (should= {} (GenericRecordValidator {:first-name "Guy" :zipcode ""}))
      (should (:first-name (GenericRecordValidator {:first-name nil :zipcode "12345"}))))

    )

  )