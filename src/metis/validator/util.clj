(ns metis.validator.util
  (:use [clojure.string :only [blank? replace replace-first lower-case] :rename {blank? str-blank? replace str-replace}]))

(defn blank? [attr]
  (cond
    (string? attr) (str-blank? attr)
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

(defn keyword->str [k]
  (str (name k)))

(defn in? [item coll]
  (some #(= item %) coll))

(def capital #"[A-Z]")
(defn spear-case [s]
  (let [s (or (replace-first s capital (fn [c] (lower-case c))) s)]
    (or (str-replace s capital (fn [c] (str "-" (lower-case c)))) s)))