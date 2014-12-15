(ns metis.util-spec
  (:require [speclj.core :refer :all]
            [metis.util :refer :all]))

(describe "utility functions"

  (context "blank?"
    (it "returns true for an empty string"
      (should (blank? "")))

    (it "returns true for an empty collection"
      (should (blank? [])))

    (it "returns false for a non-empty string"
      (should-not (blank? "here")))

    (it "returns false for a non-empty collection"
      (should-not (blank? ["here"])))

    )

  )
