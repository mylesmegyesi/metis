(ns model.core
  (:use [clojure.string :only [lower-case]]))


(defmacro defmodel [model-name & opts]
  (let [model-str (lower-case (str model-name))
        new-fn-name (symbol (str "new-" model-str))]
    `(defn ~new-fn-name [attrs#] attrs#)))

