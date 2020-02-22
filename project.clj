(defproject tmx "0.2.1"
  :description "Tmux session management tool"
  :url "https://github.com/narkisr/tmuxificator"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
      [org.clojure/clojure "1.10.1"]

      ; <<
      [org.clojure/core.incubator "0.1.4"]

      ; repl
      [org.clojure/tools.namespace "0.3.1"]

      ; configuration load
      [org.clojure/tools.reader "1.3.2"]

      ; version check
      [org.martinklepsch/clj-http-lite "0.4.1"]
      [org.clojure/data.json "0.2.6"]

      ; cli parsing
      [cli-matic "0.3.8"]

      ; spec validation errors
      [expound "0.7.2"]
    ]

  :plugins [
     [lein-cljfmt "0.6.3"]
     [lein-ancient "0.6.15" :exclusions [org.clojure/clojure]]
     [lein-tag "0.1.0"]
     [lein-set-version "0.3.0"]
   ]

  :profiles {
    :dev {
      :set-version {
        :updates [
            {:path "src/tmx/core.clj" :search-regex #"\"\d+\.\d+\.\d+\""}
            {:path "bin/binary.sh" :search-regex #"\d+\.\d+\.\d+"}
            {:path "README.md" :search-regex #"\d+\.\d+\.\d+"}
        ]
      }

      :repl-options {
        :init-ns user
        :timeout 120000
      }
    }

    :uberjar {
       :aot :all
    }
  }


  :aliases {
     "travis" [ "do" "clean," "compile," "cljfmt" "check" ]
  }

  :main ^:skip-aot tmx.core
)
