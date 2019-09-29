(defproject tmx "0.1.0"
  :description "Tmux session management tool"
  :url "https://github.com/narkisr/tmuxificator"
  :license  {:name "Apache License, Version 2.0" :url "http://www.apache.org/licenses/LICENSE-2.0.html"}
  :dependencies [
      [org.clojure/clojure "1.10.1"]

      ; <<
      [org.clojure/core.incubator "0.1.4"]

      [me.raynes/fs "1.4.6"]

      ; repl
      [org.clojure/tools.namespace "0.3.1"]

      [org.clojure/tools.reader "1.3.2"]

      [com.taoensso/timbre "4.10.0"]

      [org.martinklepsch/clj-http-lite "0.4.1"]
      [org.clojure/data.json "0.2.6"]

      [org.bouncycastle/bcprov-jdk15on "1.61"]
      [org.bouncycastle/bctls-jdk15on "1.61"]
    ]

  :plugins [
     [io.taylorwood/lein-native-image "0.3.1"]
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
       :native-image {:jvm-opts ["-Dclojure.compiler.direct-linking=true"]}
    }
  }


  :aliases {
     "travis" [ "do" "clean," "compile," "cljfmt" "check" ]
  }

  :native-image {
      :name "tmx"
      :graal-bin "/opt/graalvm-ce-19.2.0.1/bin/native-image"
      :opts [
        "--no-server"
        "--report-unsupported-elements-at-runtime"
        "--initialize-at-build-time"
        "-J-Djava.security.properties=java.security.overrides"
        "-H:EnableURLProtocols=https"
        "--verbose"]
  }

  :main ^:skip-aot tmx.core
)
