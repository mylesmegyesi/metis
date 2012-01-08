(require '[summary])

(defproject metis summary/version
  :description "An ORM"
  :url ~summary/url
  :license ~summary/license
  :dependencies [[org.clojure/clojure "1.4.0-alpha3"]
                 [metis/validator ~summary/version]
                 [metis/model ~summary/version]]
  :dev-dependencies [[lein-sub "0.1.2"]]
  :sub
  ["model"
   "validator"])