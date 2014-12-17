(ns metis.util
  (:require [clojure.string :as str]))

(defn blank? [attr]
  (cond
    (string? attr) (str/blank? attr)
    (coll? attr) (empty? attr)
    :else false))

(defn present? [attr]
  (not (or (blank? attr) (nil? attr))))

(defn formatted? [attr pattern]
  (when (nil? pattern)
    (throw (#+clj Exception. #+cljs js/Error. "Pattern to match with not given.")))
  (when (not (nil? attr))
    (not (nil? (re-matches pattern attr)))))