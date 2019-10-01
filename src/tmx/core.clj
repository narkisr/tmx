(ns tmx.core
  (:gen-class)
  (:require
   [clojure.string :as s]
   [clojure.data.json :as json]
   [clj-http.lite.client :as client]
   [clojure.java.io :as io]
   [clojure.edn :as edn]
   [clojure.core.strint :refer  (<<)]
   [clojure.java.shell :refer (sh)])
  (:import java.lang.Integer))

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

(def tmux "/usr/bin/tmux")

(defn list-sessions []
  (letfn [(into-session [st]
            (let [[k data] (s/split st #":\s")
                  c (Integer/parseInt (last (re-find #"(\d+) windows.*" data)))]
              {(keyword k) {:windows c :attached (s/includes? data "(attached)")}}))]
    (let [{:keys [out exit]} (sh tmux "ls")]
      (when (= exit 0)
        (apply merge (map into-session (s/split out #"\n")))))))

(defn start-tmux [profile root {:keys [dir cmd]}]
  (sh (configuration :terminal) "-e" (<< "~{tmux} new-session -s ~{profile} ~{cmd}") :dir (or dir root)))

(defn session-launched? [profile]
  (contains? (list-sessions) (keyword profile)))

(defn wait-for-session [c profile]
  (when-not (and (session-launched? profile) (< c 3))
    (Thread/sleep 1000)
    (wait-for-session (+ 1 c) profile)))

(defn launch
  [profile]
  {:pre [(string? profile)]}
  (let [{:keys [root windows]} (configuration :profiles (keyword profile))
        base (first windows)]
    (start-tmux profile root base)
    (wait-for-session 0 profile)
    (when-not (session-launched? profile)
      (stderr "failed to start session")
      (exit 1))
    (doseq [{:keys [cmd dir]} (rest windows)]
      (sh tmux "new-window" "-t" (<< "~{profile}:0") cmd :dir (or dir root)))))

(defn -main [& args]
  (try
    (case (first args)
      "start" (do (launch (second args)) (exit 0))
      "version" (version)
      "help" (help-)
      nil (help-))
    (catch Exception e
      (stderr e)
      (exit 1))))
