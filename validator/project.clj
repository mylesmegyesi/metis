(require '[summary])

(defproject metis/validator summary/version
  :description "A library for validations."
  :url ~summary/url
  :license ~summary/license
  :dependencies [[org.clojure/clojure "1.4.0-alpha3"]]
  :dev-dependencies [[speclj "2.0.0"]]
  :test-path "spec/")