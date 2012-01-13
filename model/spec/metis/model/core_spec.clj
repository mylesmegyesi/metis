(ns metis.model.core-spec
  (:use [speclj.core]
    [metis.model.core]
    [metis.validator.core]))

(defmodel Customer
  (:attributes
    :first-name :last-name)
  (:validations
    (:first-name :presence {:message "Testing"})
    (:last-name :presence)))

;(defmodel Customer
;  (:attributes
;    :company-name :notes)
;  (:validations
;    (:company-name :presence))
;  (:associations
;    :has-one :address
;    :has-one :contact
;    :has-many :job-sites))
;;
;(defmodel Address
;  (:attributes
;    :addr-line-1 :addr-line-2 :city :state :zipcode :country)
;  (:validations
;    ([:addr-line-1 :city :state :zipcode :country] :presence)))

;(defmodel Contact
;  (:attributes
;    :first-name :last-name :phone-number :fax-number :email)
;  (:validations
;    ([:first-name :last-name :phone-number] :presence)
;    (:phone-number :phone-number)
;    (:fax-number :phone-number {:allow-nil true :allow-blank true})
;    (:email :email {:allow-nil true :allow-blank true})))
;
;(definteractor Customer
;  (:data-store :in-memory)
;  (:validates-associated)
;  (:accept-nested-attributes [:address :contact]))

;(definteractor Address)

;(use '[hosemonster.customer :only [save find-where]])
;
;(save {:company-name "Acme, Inc." :address {:addr-line-1 "1234 1st St." :addr-line-2 "Suite 1000" :city "Chicago" :state "IL" :zipcode "12345" :country "US"}})

;(find-where {:company-name "Acme, Inc."})

;[{:id 1 :company-name "Acme, Inc." :address {:id 1 :addr-line-1 "1234 1st St." :addr-line-2 "Suite 1000" :city "Chicago" :state "IL" :zipcode "12345" :country "US"}}]


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
