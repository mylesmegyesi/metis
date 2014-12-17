(ns metis.support.addable)

(defprotocol Addable
  [plus [this other]])

(deftype Dummyaddable []
  Addable
  (plus [this other] nil))

(defn new-dummyaddable []
  (Dummyaddable.))
