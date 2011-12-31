(ns metis.model.core-spec
  (:use [speclj.core]
    [metis.model.core :only [defmodel]]))

(defmodel Customer
  (:attributes
    :first-name :last-name)
  (:validations
    (:first-name :is-present)
    (:last-name :is-present)))

(describe "model"

  (it "has the given attributes"
    (should= "Jimmy" (:first-name (new-customer {:first-name "Jimmy"})))
    (should-not= "Jimmy" (:customer-name (new-customer {:customer-name "Jimmy"}))))

  (it "has a validator"
    (should= {} (customer-errors {:first-name "Jimmy" :last-name "John's"}))
    (should= {} (customer-errors {:customer-name "Jimmy"})))

  )