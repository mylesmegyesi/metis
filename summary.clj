(ns summary
  (:require
    [clojure.string :as str]))

(def major 0)
(def minor 1)
(def tiny 3)
(def snapshot true)
(def version
  (str
    (str/join "." (filter identity [major minor tiny]))
    (if snapshot "-SNAPSHOT" "")))

(def license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"})

(def url "https://github.com/mylesmegyesi/metis")