(require '[summary])

(defproject metis/data-store summary/version
  :description "A library for storing data."
  :url ~summary/url
  :license ~summary/license
  :dependencies [[org.clojure/clojure "1.4.0-alpha3"]]
  :dev-dependencies [[speclj "2.0.0"]]
  :test-path "spec/")