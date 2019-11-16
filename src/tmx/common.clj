(ns tmx.common)

(defn stderr [e]
  (binding [*out* *err*]
    (println e)))

(defn exit [c]
  (System/exit c))

