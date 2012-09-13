(defproject metis "0.1.7"
  :description "A library for validating maps in Clojure."
  :url "https://github.com/mylesmegyesi/metis"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}

  :dependencies [[org.clojure/clojure "1.4.0"]]

  ; leiningen 1
  :dev-dependencies [[speclj "2.3.0"]]
  :test-path "spec"

  ; leiningen 2
  :profiles {:dev {:dependencies [[speclj "2.3.0"]]}}
  :test-paths ["spec/"]
  :plugins [[speclj "2.3.0"]])
