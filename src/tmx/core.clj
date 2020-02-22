(ns tmx.core
  (:gen-class)
  (:require
   [tmx.rendering :refer (render)]
   [tmx.config :refer (configuration)]
   [tmx.common :refer (stderr exit)]
   [clojure.string :as s :refer (join split)]
   [clojure.spec.alpha :as spec]
   [cli-matic.core :refer (run-cmd)]
   [clojure.data.json :as json]
   [clj-http.lite.client :as client]
   [clojure.core.strint :refer  (<<)]
   [clojure.java.shell :refer (sh)])
  (:import java.lang.Integer))

(defn tags [user repo]
  (json/read-str
   (:body (client/get (<< "https://api.github.com/repos/~{user}/~{repo}/tags"))) :key-fn keyword))

(defn version [_]
  (let [current "0.2.0" last-version (:name (last (sort-by :name (tags "narkisr" "tmx"))))]
    (if-not (= current last-version)
      (println (<< "tmx version is ~{current} the latest version is ~{last-version} please upgrade"))
      (println "tmx" current))))

(def tmux "/usr/bin/tmux")

(defn list-sessions []
  (letfn [(into-session [st]
            (let [[k data] (split st #":\s")
                  c (Integer/parseInt (last (re-find #"(\d+) windows.*" data)))]
              {(keyword k) {:windows c :attached (s/includes? data "(attached)")}}))]
    (let [{:keys [out exit]} (sh tmux "ls")]
      (when (= exit 0)
        (apply merge (map into-session (split out #"\n")))))))

(defn start-tmux [profile root cmd new?]
  (try
    (if new?
      (sh (configuration :terminal) "-e" (<< "~{tmux} new-session -s ~{profile} \\; ~(join \" \" cmd)") :dir root)
      (apply sh (concat (list tmux  "new-session" "-d" "-s" profile ";") cmd (list :dir root))))
    (catch Exception e
      (stderr (.getMessage e))
      (exit 1))))

(defn session-launched? [profile]
  (contains? (list-sessions) (keyword profile)))

(defn wait-for-session [c profile]
  (when-not (and (session-launched? profile) (< c 3))
    (Thread/sleep 1000)
    (wait-for-session (+ 1 c) profile)))

(defn load-profile [profile]
  (configuration :profiles (keyword profile)))

(defn launch
  [profile new?]
  {:pre [(string? profile)]}
  (let [{:keys [root windows]} (load-profile profile)
        rendering (render windows root new?)]
    (let [{:keys [err out code]} (start-tmux profile root rendering new?)]
      (when-not (= 0 code)
        (stderr out)
        (exit 1)))
    (when-not (session-launched? profile)
      (stderr "Failed to launch session")
      (exit 1))
    (when-not new?
      (println profile))))

(defn output-rendering
  [{:keys [p]}]
  {:pre [(string? p)]}
  (let [{:keys [root windows]} (load-profile p)
        rendering (render windows root false)]
    (doseq [line (split (join " " rendering) #";\s?")]
      (println line))))

(defn launch-n-exit [{:keys [p n]}]
  (launch p (Boolean/parseBoolean n))
  (exit 0))

(spec/def ::bool #{"true" "false"})

(def cli
  {:app {:command     "tmx"
         :description "tmux session managment"
         :version     "0.2.0"}

   :global-opts []

   :commands    [{:command     "start"
                  :description "Launch a new tmux session with profile"
                  :opts        [{:option "p" :as "Profile" :type :string}
                                {:option "n" :as "New window" :type :string :default "false" :spec ::bool}]
                  :runs        launch-n-exit}

                 {:command     "render"
                  :description "Output the rendering of the select profile"
                  :opts        [{:option "p" :as "Profile" :type :string}]
                  :runs        output-rendering}

                 {:command     "version"
                  :description "Show version and check if its the latest"
                  :runs        version}]})

(defn -main [& args]
  (try
    (run-cmd args cli)
    (catch Exception e
      (stderr e)
      (exit 1))))

(comment
  (output-rendering {:p "re-core"}))
