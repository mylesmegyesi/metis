(ns metis.util-spec
  (#+clj :require #+cljs :require-macros [speclj.core :refer [describe context it should should-not]])
  (:require [metis.util :refer [blank?]]
            [speclj.core]))

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
