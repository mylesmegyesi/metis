(ns map-validator.core-spec
  (:use [speclj.core]
    [map-validator.core :only [validate validate-attr]]
    [map-validator.validations :only [with] :rename {with my-with}]))

(def mock-with-called-count (atom 0))
(defn mock-with [attr {}]
  (swap! mock-with-called-count #(inc %)))

(describe "map validator"

  (context "validate attr"
    (it "runs the validation"
      (should-not= nil (validate-attr nil :is-present))
      (should= nil (validate-attr "spmething" :is-present))
      (should-not= nil (validate-attr nil :is-present {})))

    (it "uses the given error message"
      (let [message "other message"]
        (should= message (validate-attr nil :is-present {:message message}))))

    )

  (context "validate"
    (after
      (reset! mock-with-called-count 0))

    (it "calls each validation"
      (binding [my-with mock-with]
        (validate {:foo "foo" :bar "bar"}
          [:foo :is-present]
          [:bar :with {:validator (fn [attr] true)}]))
      (should= 2 @mock-with-called-count))

    (it "returns a map of errors"
      (let [message "error"
            errors (validate {:foo "" :bar "bar"}
          [:foo :is-present {:message message}]
          [:bar :with {:validator (fn [attr] false) :message message}])]
        (should= {:foo message :bar message} errors)))

    (it "does not run validations for a key after it has already failed"
      (binding [my-with mock-with]
        (let [message "error"
              errors
              (validate {:foo ""}
                [:foo :is-present {:message message}]
                [:foo :with {:validator (fn [attr] true)}])]
          (should= 1 @mock-with-called-count))))

    )

  )