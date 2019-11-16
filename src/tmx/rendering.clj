(ns tmx.rendering
  "Tmux rendering functions"
  (:require
   [clojure.core.strint :refer  (<<)]))

(defn directions [k c]
  (k {:verticle (map  (fn [_] (list "split-window" "-v" ";")) (range c))
      :horizontal (map  (fn [_] (list "split-window" "-h" ";")) (range c))
      :alternating (map  (fn [d] (list "split-window" d ";")) (take c (cycle ["-h" "-v"])))}))

(defn send-keys [new? {:keys [cmd]}]
  (let [cmd' (if new? (<< "\"~{cmd}\"") cmd)]
    (list "send-keys" cmd' "C-m" ";")))

(defn new-window [cmd root]
  (concat (list "new-window" "-c" root ";") cmd))

(defn select-layout [k]
  (list "select-layout" (name k) ";"))

(defn reneder-window [{:keys [layout split panes]} new?]
  (concat
   (butlast
    (interleave
     (map (partial send-keys new?) panes)
     (directions split (count panes))))
   (list (select-layout layout))))

(defn select-window [i]
  (list "select-window" "-t" (str i) ";"))

(defn render
  [windows root new?]
  (flatten
   (concat
    (cons (reneder-window (first windows) new?)
          (map (fn [{:keys [dir] :as w}]
                 (new-window (reneder-window w new?) (or dir root))) (rest windows)))
    (select-window 0))))

