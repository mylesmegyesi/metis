(require '[summary])

(defproject metis/model summary/version
  :description "A library for models."
  :url ~summary/url
  :license ~summary/license
  :dependencies [[org.clojure/clojure "1.4.0-alpha3"]
                 [metis/validator ~summary/version]]
  :dev-dependencies [[speclj "2.0.0"]]
  :test-path "spec/")