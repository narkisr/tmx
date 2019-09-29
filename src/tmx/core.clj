(ns tmx.core
  (:gen-class)
  (:require
   [clojure.data.json :as json]
   [clj-http.lite.client :as client]
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [clojure.core.strint :refer  (<<)]
   [clojure.java.shell :refer (sh)]))

(defn tags [user repo]
  (json/read-str
   (:body (client/get (<< "https://api.github.com/repos/~{user}/~{repo}/tags"))) :key-fn keyword))

(defn version []
  (let [current  "0.1.0" last-version (:name (last (sort-by :name (tags "narkisr" "tmx"))))]
    (if-not (= current last-version)
      (println (<< "tmx version is ~{current} the latest version is ~{last-version} please upgrade"))
      (println "tmx" current))))

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

(defn help- []
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
      (sh "/usr/bin/tmux" "new-window" "-t" (<< "~{profile}:0") cmd :dir (or dir root)))
    (exit 0)))

(defn -main [& args]
  (try
    (case (first args)
      "start" (launch (second args))
      "version" (version)
      "help" (help-)
      nil (help-))
    (catch Exception e
      (stderr e)
      (exit 1))))
