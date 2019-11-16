(ns tmx.config
  (:require
   [clojure.core.strint :refer  (<<)]
   [tmx.common :refer (stderr exit)]
   [clojure.edn :as edn]
   [clojure.java.io :as io]))

(defn configuration
  ([]
   (let [f (str (System/getProperty "user.home") "/.tmx.edn")]
     (when-not (.exists (io/file f))
       (stderr (<< "~{f} configuration file is missing"))
       (exit 1))
     (edn/read-string (slurp f))))
  ([& ks] (get-in (configuration) ks)))
