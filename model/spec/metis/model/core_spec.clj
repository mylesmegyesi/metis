(ns metis.model.core-spec
  (:use [speclj.core]
    [metis.model.core :only [defmodel]]))

(defmodel Customer
  (:attributes
    :first-name :last-name)
  (:validations
    [:first-name :is-present {:message "Testing"}]
    [:last-name :is-present]))

(describe "model"

  (describe "attributes"

    (it "has the given attributes"
      (should= "Jimmy" (:first-name (new-customer {:first-name "Jimmy"}))))

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
