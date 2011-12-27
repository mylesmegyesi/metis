(ns map-validator.core-spec
  (:use [speclj.core]
    [map-validator.core :only [validate build-message stringify-keyword]]))

(describe "map validator"

  (context "stringify keyword"

    (it "gives correct error message with dash in the keyword"
      (should= "First name" (stringify-keyword :first-name)))

    (it "gives correct error message with underscore in the keyword"
      (should= "First name" (stringify-keyword :first_name)))

    )

  (context "validate"
    (it "runs the validation"
      (should-not= nil (validate {} :key :is-present {})))

    (it "uses the given error message"
      (let [message "other message"]
        (should= (build-message (stringify-keyword :key) message) (validate {} :key :is-present {:message message}))))

    (it "uses the given key name"
      (let [message "other message"
            key-name "other key"]
        (should= (build-message key-name message) (validate {} :key :is-present {:message message :key-name key-name}))))

    )

  )