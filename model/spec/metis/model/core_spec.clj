(ns metis.model.core-spec
  (:use [speclj.core]
    [metis.model.core]
    [metis.validator.core]))

(defmodel Customer
  (:attributes
    :first-name :last-name)
  (:validations
    (validate :first-name :presence {:message "Testing"})
    (validate :last-name :presence)))

(describe "model"

  (describe "attributes"

    (it "has a validator"
      (should= {} (customer-errors {:first-name "Jimmy" :last-name "John's"}))
      (should= 2 (count (customer-errors {}))))

    (it "does not accept attributes not given"
      (should-not= "Jimmy" (:customer-name (new-customer {:customer-name "Jimmy"}))))
    )

  (describe "validator"

    (it "has a validator"
      (should (contains? (customer-errors (new-customer {:first-name "Jimmy"})) :last-name)))

    (it "has a validation failure message"
      (should= {:first-name ["Testing"]} (customer-errors (new-customer {:last-name "Dave"}))))
    )
  )
