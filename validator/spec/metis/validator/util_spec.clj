(ns metis.validator.util-spec
  (:use
    [speclj.core]
    [metis.validator.util]))

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

  (context "spear case"
    (it "converts a string to spear case"
      (should= "thing" (spear-case "thing"))
      (should= "thing" (spear-case "Thing"))
      (should= "thing-thingy" (spear-case "ThingThingy")))

    )

  )