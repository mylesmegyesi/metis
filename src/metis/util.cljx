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

(defn str->int [s]
  #+clj
  (try
    (Integer. s)
    (catch NumberFormatException e))
  #+cljs
  (js/parseInt s)
  )

(defn str->float [s]
  #+clj
  (try
    (Float. s)
    (catch NumberFormatException e))
  #+cljs
  (js/parseFloat s))

;(defprotocol Includable
;  (includes? [this item]))
;
;(extend-protocol Includable
;  #+clj clojure.lang.Seqable
;  #+cljs cljs.core.ISeqable
;  (includes? [this item]
;    (not (nil? (some #(= item %) this)))))

; Hack because above doesn't seem to work
(defn includes? [s item]
  (not (nil? (some #(= item %) s))))

