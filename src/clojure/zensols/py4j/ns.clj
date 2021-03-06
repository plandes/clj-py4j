(ns ^{:doc "This is the default namespace that a Python `Clojure` instance
starts in.  The idea is to add all root variable bindings to this namespace and
have the convenience of the namespace macros.

Namespace macros taken from
[clojure/clojure-contrib](https://clojure.github.io/clojure-contrib/with-ns-api.html)."}
    zensols.py4j.ns)

(defmacro with-ns
  "Evaluates body in another namespace.  ns is either a namespace
  object or a symbol.  This makes it possible to define functions in
  namespaces other than the current one."
  [ns & body]
  `(binding [*ns* (the-ns ~ns)]
     ~@(map (fn [form] `(eval '~form)) body)))

(defmacro with-temp-ns
  "Evaluates body in an anonymous namespace, which is then immediately
  removed.  The temporary namespace will 'refer' clojure.core."
  [& body]
  `(try
    (create-ns 'sym#)
    (let [result# (with-ns 'sym#
                    (clojure.core/refer-clojure)
                    ~@body)]
      result#)
    (finally (remove-ns 'sym#))))

(defn stringify-keys
  "Converts all keywords to strings for all keys in **m**."
  [m]
  (->> m
       (map (fn [[k v]]
              {(name k) v}))
       (apply merge)))
