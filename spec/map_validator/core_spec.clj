(ns map-validator.core-spec
  (:use [speclj.core]
    [map-validator.core :only [validate-attr stringify-keyword]]))

(describe "map validator"

  (context "stringify keyword"

    (it "gives correct error message with dash in the keyword"
      (should= "First name" (stringify-keyword :first-name)))

    (it "gives correct error message with underscore in the keyword"
      (should= "First name" (stringify-keyword :first_name)))

    )

  (context "validate attr"
    (with default-message "default")
    (with mock-false-validation (fn [attrs attr args] {:result false :error-message @default-message}))
    (with mock-true-validation (fn [attrs attr args] {:result true}))


    (it "runs the validation"
      (should (:result (validate-attr {} :key @mock-true-validation {}))))

    (it "uses the default error message if the given is nil"
      (should= (str (stringify-keyword :key) " " @default-message) (:error-message (validate-attr {} :key @mock-false-validation {}))))

    (it "uses the given error message if not nil"
      (let [given-message "error!!!!"]
        (should= (str (stringify-keyword :key) " " given-message) (:error-message (validate-attr {} :key @mock-false-validation {:error-message given-message})))))

    (it "accepts built-in validations as keywords"
      (should-not (:result (validate-attr {} :key :is-present? {}))))

    )

  (context "validate"

    )

  )