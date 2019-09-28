(ns tmuxificator.core
  (:gen-class)
  (:require
   [clojure.java.shell :as sh]))

(defn help []
  (println "
Usage:
  tmx start  {profile}  - start a tmux profile
  tmx version         - print and check latest version.
  tmx help            - print this help message.
"))

(defn -main [& args]
  (try
    (case (first args)
      "start" (println "starting")
      "help" (help)
      nil (help))
    (catch Exception e
      (println e)
      (System/exit

       1))))


