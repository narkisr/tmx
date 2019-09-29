(ns tmuxificator.core
  (:gen-class)
  (:require
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [clojure.core.strint :refer  (<<)]
   [clojure.java.shell :refer (with-sh-dir sh)]))

(defn stderr [e]
  (binding [*out* *err*]
    (println e)))

(defn exit [c]
  (System/exit c))

(defn configuration
  ([]
   (let [f (str (System/getProperty "user.home") "/.tmx.edn")]
     (when-not (.exists (io/file f))
       (stderr (<< "~{f} configuration file is missing"))
       (exit 1))
     (edn/read-string (slurp f))))
  ([& ks] (get-in (configuration) ks)))

(defn help []
  (println "
Usage:
  tmx start {profile}  - start a tmux profile
  tmx version         - print and check latest version.
  tmx help            - print this help message.
"))

(defn start-tmux [profile root {:keys [dir cmd]}]
  (sh (configuration :terminal) "-e" (<< "/usr/bin/tmux new-session -s ~{profile} ~{cmd}") :dir (or dir root)))

(defn launch
  [profile]
  {:pre [(string? profile)]}
  (let [{:keys [root windows]} (configuration :profiles (keyword profile))
        base (first windows)]
    (start-tmux profile root base)
    (doseq [{:keys [cmd dir]} (rest windows)]
      (sh "/usr/bin/tmux" "new-window" "-t" (<< "~{profile}:0") cmd :dir (or dir root)))))

(defn -main [& args]
  (try
    (case (first args)
      "start" (launch (second args))
      "help" (help)
      nil (help))
    (catch Exception e
      (stderr e)
      (exit 1))))
