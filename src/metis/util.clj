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
    (throw (Exception. "Pattern to match with not given.")))
  (when (not (nil? attr))
    (not (nil? (re-matches pattern attr)))))

(defn str->int [s]
  (try
    (Integer. s)
    (catch NumberFormatException e)))

(defn str->float [s]
  (try
    (Float. s)
    (catch NumberFormatException e)))

(defprotocol Includable
  (includes? [this item]))

(extend-protocol Includable
  clojure.lang.Seqable
  (includes? [this item]
    (not (nil? (some #(= item %) this)))))

