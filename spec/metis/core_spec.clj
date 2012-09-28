(ns metis.core-spec
  (:use
    [speclj.core :rename {with other-with}]
    [metis.core]))

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

(describe "validator"
  (context "run validation"
    (it "runs the validation"
      (should-not= nil (-run-validation {:foo nil} :foo :presence {}))
      (should= nil (-run-validation {:foo "something"} :foo :presence {})))

    (it "uses the given error message"
      (let [message "other message"]
        (should= message (-run-validation {:foo nil} :foo :presence {:message message}))))

    (it "allows nil"
      (should= nil (-run-validation {:foo nil} :foo :presence {:allow-nil true})))

    (it "allows blank"
      (should= nil (-run-validation {:foo ""} :foo :presence {:allow-blank true})))

    (it "allows absence"
      (should= nil (-run-validation {:foo ""} :foo :presence {:allow-absence true}))
      (should= nil (-run-validation {:foo nil} :foo :presence {:allow-absence true})))

    (it "runs validations on create and update by default"
      (should-not= nil (-run-validation {:foo ""} :foo :presence {} :create))
      (should-not= nil (-run-validation {:foo ""} :foo :presence {} :update)))

    (it "uses create as the default context"
      (should= nil (-run-validation {:foo ""} :foo :presence {:on :update})))

    (it "doesn't run validations if on is set to create and context is update"
      (let [called-count (atom 0)]
        (with-redefs [presence-validator (fn [& _] (swap! called-count #(inc %)))]
          (should= nil (-run-validation {:foo ""} :foo :presence {:on :create} :update))
          (should= 0 @called-count))))

    (it "doesn't run validations if on is set to update and context is create"
      (let [called-count (atom 0)]
        (with-redefs [presence-validator (fn [& _] (swap! called-count #(inc %)))]
          (should= nil (-run-validation {:foo ""} :foo :presence {:on :update} :create))
          (should= 0 @called-count))))

    (it "on can be a singular or collection"
      (should= nil (-run-validation {:foo "something"} :foo :presence {:on :create}))
      (should= nil (-run-validation {:foo "something"} :foo :presence {:on [:create]})))

    )

  (context "run validations"
    (it "runs the validations"
      (should= 1 (count (-run-validations {:foo nil} :foo [[:presence {}]])))
      (should= () (-run-validations {:foo "something"} :foo [[:presence {}]]))
      (should= 1 (count (-run-validations {:foo "som"} :foo [[:length {:equal-to 4}] [:presence {}]]))))
    )

  (context "parsing"

    (it "-parse-attributes: converts attributes into a collection of attributes"
      (should= [:attr1] (-parse-attributes :attr1))
      (should= [:attr1 :attr2] (-parse-attributes [:attr1 :attr2])))

    (it "-parse-validations: converts validations into a collection of attributes"
      (should= [[:validator-name {}]] (-parse-validations [:validator-name]))
      (should= [[:validator-name {}]] (-parse-validations [:validator-name {}]))
      (should= [[:validator-name {}] [:other-validator {}]] (-parse-validations [:validator-name {} :other-validator]))
      (should= [[:validator-name {}] [:other-validator {}]] (-parse-validations [:validator-name {} :other-validator {}])))

    (for [params [[:attr1 :validation1] [:attr1 :validation1 {}] [:attr1 [:validation1]] [[:attr1] :validation1]]]
      (it (str "-parse: returns [[:attr1] [[:validation1 {}]]] for parameters " params)
        (should= [[:attr1] [[:validation1 {}]]] (apply -parse params))))

    )

  (context "expand validations"
    (it "-expand-validation"
      (should= {:attr #{[:validation1 {}]}} (-expand-validation [:attr [:validation1]]))
      (should= {:attr #{[:validation1 {}]}} (-expand-validation [:attr [:validation1 :validation1]]))
      (should= {:attr #{[:validation1 {}]}} (-expand-validation [:attr [:validation1 {} :validation1]]))
      (should= {:attr #{[:validation1 {}]}} (-expand-validation [:attr [:validation1 {} :validation1 {}]]))
      (should= {:attr #{[:validation1 {}] [:validation2 {}]}} (-expand-validation [:attr [:validation1 {} :validation2]])))

    (it "-expand-validations"
      (should= {:attr #{[:validation1 {}]}} (-expand-validations [[:attr [:validation1]]]))
      (should= {:attr #{[:validation1 {}] [:validation2 {}]}} (-expand-validations [[:attr [:validation1]] [:attr [:validation2]]]))
      (should= {:attr #{[:validation1 {}]}} (-expand-validations [[:attr [:validation1]] [:attr [:validation1]]])))

    )

  (context "defvalidator"
    (it "defines a validator"
      (should= {} (generic-record-validator {:first-name "Guy" :zipcode ""}))
      (should (:first-name (generic-record-validator {:first-name nil :zipcode "12345"}))))

    (it "defines a validator with with a string"
      (should= {} (other-validator {:first-name "Guy" :zipcode ""})))

    (it "defines a validator with with a symbol"
      (should= {} (other-one-validator {:first-name "Guy" :zipcode ""})))

    (it "does not re-append validator"
      (should= {} (already-validator {:first-name "Guy" :zipcode ""})))

    (it "handles nested maps with no errors"
      (should= {} (person-validator {:first-name "name" :address {:line-1 "1" :line-2 "2" :zipcode "64521" :nation {:name "USA" :code 1}}})))

    (it "handles nested maps with errors"
      (should= {:first-name '("must be present") :address {:nation {:name '("must be present"), :code '("must be present")} :zipcode '("must be present"), :line-1 '("must be present") :line-2 '("must be present")}} (person-validator {})))

           )

  (context "validate"
    (it "validates an individual record"
      (let [valid-record {:first-name "Guy" :zipcode ""}
            invalid-record {:first-name nil :zipcode "12345"}
            vaidations {:zipcode #{[:presence {:allow-blank true}]}, :first-name #{[:presence {:allow-nil true}] [:presence {:allow-blank true}]}}]
        (should= {} (validate valid-record vaidations))
        (should (:first-name (validate invalid-record vaidations)))))
    )

  (context "utils"
    (it "-remove-nil: removes all nil entries"
      (should= [] (-remove-nil [nil]))
      (should= [10] (-remove-nil [10])))

    (it "-merge-validations: merges maps that have sets for values"
      (should= {:a #{:a :b} :b #{:b :c}} (-merge-validations [{:a #{:a :b}} {:b #{:b :c}}]))
      (should= {:a #{:a :b :c}} (-merge-validations [{:a #{:a :b}} {:a #{:b :c}}]))
      (should= {} (-merge-validations [])))))
