(ns map-validator.core-spec
  (:use [speclj.core]
    [map-validator.core :only [run-validation error-message stringify-keyword]]))

(describe "map validator"

  (context "run validation"
    (it "runs the validation"
      (should (:result (run-validation (fn [attrs attr args] {:result true}) {} :key {}))))

    (it "uses the default error message if the given is nil"
      (let [default-message "error!!!!"]
        (should= (error-message :key nil default-message) (:error-message (run-validation (fn [attrs attr args] {:result false :error-message default-message}) {} :key {})))))

    (it "uses the given error message if not nil"
      (let [default-message "default"
            given-message "error!!!!"]
        (should= (error-message :key given-message default-message) (:error-message (run-validation (fn [attrs attr args] {:result false :error-message default-message}) {} :key {:error-message given-message})))))

    )

  (context "stringify keyword"

    (it "gives correct error message with dash in the keyword"
      (should= "First name" (stringify-keyword :first-name)))

    (it "gives correct error message with underscore in the keyword"
      (should= "First name" (stringify-keyword :first_name)))

    )

  (context "error message"

    (it "uses the given message if it isn't nil"
      (should= "First name some message" (error-message :first-name "some message" nil)))

    (it "uses the default if the given is nil"
      (should= "First name default message" (error-message :first-name nil "default message")))

    )

  )