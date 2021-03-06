(ns karabiner-configurator.misc
  (:require [clojure.java.io :as io]
            [cheshire.core :as json]
            [clojure.edn :as edn]))

(def nn? "not nil" (complement nil?))

(defn which?
  "Checks if any of elements is included in coll and says which one
  was found as first. Coll can be map, list, vector and set"
  [coll & rest]
  (let [ncoll (if (map? coll) (keys coll) coll)]
    (reduce
     #(or %1  (first (filter (fn [a] (= a %2))
                             ncoll))) nil rest)))
(defn contains??
  [coll rest]
  (nn? (which? coll rest)))

(defn load-edn
  "Load edn from an io/reader source (filename or io/resource)."
  [source & args]
  (with-open [r (io/reader source)]
    (if args
      (get-in (edn/read (java.io.PushbackReader. r)) args)
      (edn/read (java.io.PushbackReader. r))))
  #_(try
      (with-open [r (io/reader source)]
        (if args
          (get-in (edn/read (java.io.PushbackReader. r)) args)
          (edn/read (java.io.PushbackReader. r))))
      (catch java.io.IOException e
        (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
      (catch RuntimeException e
        (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))

(defn load-result-edn
  [source]
  (load-edn source :result))

(defn load-json
  "Load json from an io/reader source (filename or io/resource)."
  [source]
  (with-open [r (io/reader source)]
    (json/parse-stream (java.io.PushbackReader. r) true))
  #_(try
      (with-open [r (io/reader source)]
        (json/parse-stream (java.io.PushbackReader. r) true))
      (catch java.io.IOException e
        (printf "Couldn't open '%s': %s\n" source (.getMessage e)))
      (catch RuntimeException e
        (printf "Error parsing edn file '%s': %s\n" source (.getMessage e)))))

(defmacro when-let*
  ([bindings & body]
   (if (seq bindings)
     `(when-let [~(first bindings) ~(second bindings)]
        (when-let* ~(drop 2 bindings) ~@body))
     `(do ~@body))))

(defn dissoc-in
  "Dissociates an entry from a nested associative structure returning a new
  nested structure. keys is a sequence of keys. Any empty maps that result
  will not be present in the new structure."
  [m [k & ks :as keys]]
  (if ks
    (if-let [nextmap (get m k)]
      (let [newmap (dissoc-in nextmap ks)]
        (if (seq newmap)
          (assoc m k newmap)
          (dissoc m k)))
      m)
    (dissoc m k)))