(ns metis.core-spec
  (:use
    [speclj.core :rename {with other-with}]
    [metis.test-validators]
    [metis.core]))

(defvalidator :generic-record
  ([:first-name :zipcode] [:presence {:allow-blank true}])
  (:first-name :presence {:allow-nil true}))

(defvalidator "other"
  ([:first-name :zipcode] :presence {:allow-nil false})
  (:first-name :presence {:allow-nil false}))

(defvalidator :unmerged-first-name
  (:first-name :presence {:allow-nil false})
  (:first-name :presence {:allow-blank false}))

(defvalidator other-one
  ([:first-name :zipcode] [:presence {:allow-blank false}])
  (:first-name :presence {:allow-nil false}))

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

(defvalidator :country
  ([:code :name] :presence))

(defvalidator :address
  ([:line-1 :line-2 :zipcode] :presence)
  (:nation :country))

(defvalidator :person
  (:address :address)
  (:first-name :presence))

(defvalidator :contextual
  (:first-name :presence {:only [:creation :update]})
  (:address :presence {:only [:saving]})
  (:nation :presence {:except [:saving]}))

(describe "validator"
  (it "defines a validator with a keyword as the name"
    (should= {} (generic-record {:first-name "Guy" :zipcode ""})))

  (it "defines a validator with with a string as the name"
    (should= {} (other {:first-name "Guy" :zipcode "12345"})))

  (it "defines a validator with with a symbol as the name"
    (should= {} (other-one {:first-name "Guy" :zipcode "12345"})))

  (it "runs validations on multiple fields"
    (let [errors (other-one {})]
      (should= 2 (count (:first-name errors)))
      (should= 1 (count (:zipcode errors)))))

  (it "merges validations for attributes when the options are the same"
    (should= 1 (count (:first-name (other {:first-name nil})))))

  (it "does not merge validations for attributes when the options are different"
    (should= 2 (count (:first-name (unmerged-first-name {})))))

  (it "can use a validator from a different file"
    (should= 1 (count (:first-name (foreign {})))))

  (it "handles nested maps with no errors"
    (should= {} (person {:first-name "name" :address {:line-1 "1" :line-2 "2" :zipcode "64521" :nation {:name "USA" :code 1}}})))

  (it "handles nested maps with errors"
    (should= {:first-name '("must be present") :address {:nation {:name '("must be present"), :code '("must be present")} :zipcode '("must be present"), :line-1 '("must be present") :line-2 '("must be present")}} (person {})))

  (context "dsl"
    (it "parses one attribute and one validator"
      (should-not= nil (:one (test-validator {}))))

    (it "parses two attributes and one validator"
      (let [errors (test-validator {})]
        (should-not= nil (:two errors))
        (should-not= nil (:three errors))))

    (it "parses two attributes and one validator with options"
      (let [errors (test-validator {})]
        (should= "m" (first (:four errors)))
        (should= "m" (first (:five errors)))))

    (it "parses two attributes and two validators"
      (let [errors (test-validator {})]
        (should-not= nil (first (:six errors)))
        (should-not= nil (first (:seven errors)))))

    (it "parses one attribute in braces and one validator with options"
      (should= "m" (first (:eight (test-validator {})))))

    (it "parses one attribute and one validator with options"
      (should= "m" (first (:nine (test-validator {})))))

    (it "parses one attribute and two validators with options"
      (should= 2 (count (:ten (test-validator {})))))

    (it "parses one attribute and two validators with both options"
      (let [errors (:eleven (test-validator {}))]
        (should= 2 (count errors))
        (should (some #(= "m" %) errors)))))

  (context "common options"

    (it "allows nil"
      (should= nil (:twelve (test-validator {}))))

    (it "allows blank"
      (should-not= nil (:thirteen (test-validator {})))
      (should= nil (:thirteen (test-validator {:thirteen ""})))
      (should= nil (:thirteen (test-validator {:thirteen []})))
      (should= nil (:thirteen (test-validator {:thirteen '()})))
      (should= nil (:thirteen (test-validator {:thirteen {}})))
      (should= nil (:thirteen (test-validator {:thirteen #{}}))))

    (it "allows absence"
      (should= nil (:fourteen (test-validator {})))
      (should= nil (:fourteen (test-validator {:fourteen ""}))))

    (it "uses the given error message"
      (should= "my message" (first (:fifteen (test-validator {}))))))

  (context "contextual validation"

    (it "runs all validations if no context is given"
      (let [errors (contextual {})]
        (should= 1 (count (:first-name errors)))
        (should= 1 (count (:address errors)))
        (should= 1 (count (:nation errors)))))

    (it "runs the validations unless they are excluded"
      (let [errors (contextual {} :creation)]
        (should= 1 (count (:first-name errors)))
        (should= 1 (count (:nation errors)))
        (should= nil (:address errors))))

    (it "only runs the validations in given context and excludes"
      (let [errors (contextual {} :saving)]
        (should= nil (:first-name errors))
        (should= nil (:nation errors))
        (should= 1 (count (:address errors)))))

    )
  )
