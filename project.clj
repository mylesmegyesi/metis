(defproject metis "1.0.0-SNAPSHOT"
  :description "A library for data validation in Clojure."
  :url "https://github.com/mylesmegyesi/metis"
  :license {:name "Eclipse Public License - v 1.0"
            :url "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments "same as Clojure"}

  :dependencies [[org.clojure/clojure "1.5.1"]
                 [commons-validator "1.4.0"]]

  :profiles {:dev {:dependencies [[speclj "2.6.1"]]
                   :test-paths ["spec"]
                   :plugins [[speclj "2.6.1"]]}}

  :scm {:name "git"
        :url "https://github.com/mylesmegyesi/metis"}

  )
