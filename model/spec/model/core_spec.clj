(ns model.core-spec
  (:use [speclj.core]
    [model.core :only [defmodel]]))

(defmodel Customer
  (:attributes :customer-name)
  (:validations
    (:first-name :is-present)
    (:last-name :is-present)))

(describe "model"

  (it "has the given attributes"
    (should= "Jimmy" (:customer-name (new-customer {:customer-name "Jimmy"}))))

  (it "has a validator"
    (should= "Jimmy" (:customer-name (new-customer {:customer-name "Jimmy"}))))

  )