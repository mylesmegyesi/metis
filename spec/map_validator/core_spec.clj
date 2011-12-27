(ns map-validator.core-spec
  (:use [speclj.core]
    [map-validator.core :only [validate-attr]]))

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

  )