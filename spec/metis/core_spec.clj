(ns metis.core-spec
  (:use
    [speclj.core :rename {with other-with}]
    [metis.test-validators]
    [metis.core]))

(describe "validator"
  (it "defines a validator"
    (should= {} (generic-record {:first-name "Guy" :zipcode ""}))
    (should (:first-name (generic-record {:first-name nil :zipcode "12345"}))))

  (it "defines a validator with with a string"
    (should= {} (other {:first-name "Guy" :zipcode ""})))

  (it "defines a validator with with a symbol"
    (should= {} (other-one {:first-name "Guy" :zipcode ""})))

  (it "does not re-append validator"
    (should= {} (already-validator {:first-name "Guy" :zipcode ""})))

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
      (should= "my message" (first (:fifteen (test-validator {})))))

           )
          )
