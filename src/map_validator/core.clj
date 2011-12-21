(ns map-validator.core
  (:use [map-validator.validations :only [get-validation]])
  (:require [clojure.contrib.str-utils2 :as str-utils]))

(defn stringify-keyword [attr]
  (str-utils/capitalize (str-utils/replace (str-utils/replace (name attr) "-" " ") "_" " "))
  )

(defn validate-attr [attrs attr validator args]
  (let [validator (if (keyword? validator) (get-validation validator) validator)
        result (validator attrs attr args)]
    (if (:result result)
      result
      (merge result {:error-message (str (stringify-keyword attr) " " (if (:error-message args) (:error-message args) (:error-message result)))})
      )))