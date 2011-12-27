(ns map-validator.core
  (:use [map-validator.validations :only [get-validation]])
  (:require [clojure.contrib.str-utils2 :as str-utils]))

(defn stringify-keyword [attr]
  (str-utils/capitalize (str-utils/replace (str-utils/replace (name attr) "-" " ") "_" " ")))

(defn build-message [key message]
  (str key " " message))

(defn validate [attrs attr validator args]
  (when-let [message ((get-validation validator) (attr attrs) args)]
    (build-message (:key-name args (stringify-keyword attr)) message)))