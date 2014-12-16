(defproject metis "1.0.0"
  :description "A library for data validation in Clojure."
  :url "https://github.com/mylesmegyesi/metis"
  :license {:name         "Eclipse Public License - v 1.0"
            :url          "http://www.eclipse.org/legal/epl-v10.html"
            :distribution :repo
            :comments     "same as Clojure"}

  :dependencies [[org.clojure/clojure "1.6.0"]
                 [commons-validator "1.4.0"]]

  :profiles {:dev {:dependencies [[speclj "3.1.0"]
                                  [com.keminglabs/cljx "0.5.0"]
                                  [org.clojure/clojurescript "0.0-2371"]]
                   :test-paths   ["target/test-classes"]
                   :plugins      [[speclj "3.1.0"]
                                  [com.keminglabs/cljx "0.5.0"]
                                  [lein-cljsbuild "1.0.3"]]}}

  :aliases {"cljs" ["do" "clean," "cljx," "cljsbuild" "once" "dev"]
            "ci" ["do" "spec," "cljs"]}

  :scm {:name "git"
        :url  "https://github.com/mylesmegyesi/metis"}

  :prep-tasks [["cljx" "once"]]

  :cljx {:builds [{:source-paths ["src"]
                   :output-path  "target/classes"
                   :rules        :clj}
                  {:source-paths ["src"]
                   :output-path  "target/classes"
                   :rules        :cljs}
                  {:source-paths ["spec"]
                   :output-path  "target/test-classes"
                   :rules        :clj}
                  {:source-paths ["spec"]
                   :output-path  "target/test-classes"
                   :rules        :cljs}]}

  :cljsbuild {:builds        {:dev {:source-paths   ["target/classes" "target/test-classes"]
                                    :compiler       {:output-to     "metis.js"
                                                     :optimizations :whitespace
                                                     :pretty-print  true}
                                    :notify-command ["phantomjs" "bin/speclj" "metis.js"]}}
              :test-commands {"test" ["phantomjs" "bin/speclj" "metis.js"]}}
  )