(ns metis.model.core-spec
  (:use [speclj.core]
    [metis.model.core]
    [metis.validator.core]))

(defmodel Customer
  (:attributes
    :first-name :last-name)
  (:validations
    (validate :first-name :presence)
    (validate :last-name :presence)))

(describe "model"

  (it "has the given attributes"
    (should= "Jimmy" (:first-name (new-customer {:first-name "Jimmy"})))
    (should-not= "Jimmy" (:customer-name (new-customer {:customer-name "Jimmy"}))))

  (it "has a validator"
    (should= {} (customer-errors {:first-name "Jimmy" :last-name "John's"}))
    (should= 2 (count (customer-errors {}))))

  )