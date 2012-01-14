(use '[config])

(defproject metis version
  :description "A collection of libraries for persisting data."
  :url ~url
  :license ~license
  :dependencies [~clojure
                 [metis/validator ~version]
                 [metis/data-store ~version]]
  :dev-dependencies [[lein-sub "0.1.2"]
                     ~speclj]
  :sub
  ["data_store"
   "validator"])