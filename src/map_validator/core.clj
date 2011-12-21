(ns map-validator.core
  (:require [clojure.contrib.str-utils2 :as str-utils]))

(defn stringify-keyword [attr]
  (str-utils/capitalize (str-utils/replace (str-utils/replace (name attr) "-" " ") "_" " "))
  )

(defn error-message [attr given-messge default-message]
  (str (stringify-keyword attr) " " (if given-messge given-messge default-message))
  )

(defn run-validation [fn attrs attr args]
  (let [result (fn attrs attr args)
        default-error-message (:error-message result)]
    (if default-error-message
      (merge result {:error-message (error-message attr (:error-message args) default-error-message)})
      result
      )

    )
  )