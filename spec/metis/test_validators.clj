(ns metis.test-validators
  (:use [metis.core :only [defvalidator]]))

(defvalidator test-validator
  (:one :presence)
  ([:two :three] :presence)
  ([:four :five] :presence {:message "m"})
  ([:six :seven] [:presence :length])
  ([:eight] :presence {:message "m"})
  (:nine :presence {:message "m"})
  (:ten [:presence :length {:greater-than 5}])
  (:eleven [:presence {:message "m"} :length {:greater-than 5}])
  (:twelve [:presence {:allow-nil true}])
  (:thirteen [:presence {:allow-blank true}])
  (:fourteen [:presence {:allow-absence true}])
  (:fifteen [:presence {:message "my message"}]))

(defvalidator "other"
  ([:first-name :zipcode] [:presence {:allow-blank true}])
  (:first-name :presence {:allow-nil true}))

(defvalidator other-one
  ([:first-name :zipcode] [:presence {:allow-blank true}])
  (:first-name :presence {:allow-nil true}))

(defvalidator :generic-record
  ([:first-name :zipcode] [:presence {:allow-blank true}])
  (:first-name :presence {:allow-nil true}))

(defvalidator already-validator
  ([:first-name :zipcode] [:presence {:allow-blank true}])
  (:first-name :presence {:allow-nil true}))

(defvalidator :country
  ([:code :name] :presence))

(defvalidator :address
  ([:line-1 :line-2 :zipcode] :presence)
  (:nation :country))

(defvalidator :person
  (:address :address)
  (:first-name :presence))
