(ns metis.validator.core-spec
  (:use [speclj.core]
    [metis.validator.core :only [validate validate-attr defvalidator]]
    [metis.validator.validations :only [is-present is-email with] :rename {with my-with}]))

(def mock-called-count (atom 0))
(defn mock [attr {}]
  (swap! mock-called-count #(inc %)))

(def mock2-called-count (atom 0))
(defn mock2 [attr {}]
  (swap! mock2-called-count #(inc %)))

(defvalidator generic-record-validator
  [:first-name :is-present]
  [:email :is-email])

(describe "validator"

  (context "validate attr"
    (it "runs the validation"
      (should-not= nil (validate-attr nil :is-present))
      (should= nil (validate-attr "something" :is-present))
      (should= "message" (validate-attr nil :is-present {:message "message"})))

    (it "uses the given error message"
      (let [message "other message"]
        (should= message (validate-attr nil :is-present {:message message}))))
  )

  (context "validate"
    (after
      (reset! mock-called-count 0)
      (reset! mock2-called-count 0))

    (it "calls each validation"
      (binding [is-present mock
                my-with mock2]
        (validate {:foo "foo" :bar "bar"}
          [:foo :is-present]
          [:bar :with {:validator (fn [attr] true)}]))
      (should= 1 @mock-called-count)
      (should= 1 @mock2-called-count))

    (it "returns a map of errors"
      (let [message "error"
            errors (validate {:foo "" :bar "bar" :baz "here"}
          [:foo :is-present {:message message}]
          [:bar :with {:validator (fn [attr] false) :message message}]
          [:baz :is-present])]
        (should= {:foo [message] :bar [message]} errors)))

    (it "runs validations for a key after it has already failed"
      (binding [is-present mock
                my-with mock2]
        (let [message "error"
              errors
              (validate {:foo ""}
                [:foo :is-present {:message message}]
                [:foo :with {:validator (fn [attr] true)}])]
          (should= 1 @mock-called-count)
          (should= 1 @mock2-called-count))))
  )

  (context "defvalidator"
    (it "defines a validator"
      (should (generic-record-validator {:first-name "Guy" :email "snap.into@slim.jim"}))
      (should (contains? (generic-record-validator {:first-name "Guy" :email "snap.into@sli"}) :email))
      (should (contains? (generic-record-validator {:email "snap.into@slim.jim"}) :first-name))
    )
  )
)
