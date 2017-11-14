(ns ^{:doc "Compiled into a Java class to help invoke Clojure functions."
      :author "Paul Landes"}
    zensols.py4j.invoke-namespace
  (:require [clojure.tools.logging :as log]
            [clojure.string :as s]
            [cemerick.pomegranate :as pom]
            [clojure.java.io :as io])
  (:gen-class
   :name "com.zensols.py4j.InvokableNamespace"
   :init init
   :state state
   :constructors {[Object] []}
   :methods [#^{:static true} [addDependency [String String String] void]
             #^{:static true} [instance [String] Object]
             #^{:static true} [instance [] Object]
             [getNamespace [] String]
             [invoke [String "[Ljava.lang.Object;"] Object]
             [eval [String java.util.Map] Object]
             [eval [String] Object]]
   :prefix "ins-"))

(def ^:private requires (atom #{}))

(def ^:private repositories
  (atom (merge cemerick.pomegranate.aether/maven-central
               {"clojars" "https://clojars.org/repo"})))

(defn- do-require [namespace]
  (log/infof "requiring: %s" namespace)
  (->> (str "(let [*ns* (find-ns (symbol \"zensols.py4j.invoke-namespace\"))] (require '[" namespace "]))")
       read-string
       eval))

(defn- maybe-require [namespace]
  (log/debugf "require: %s" namespace)
  (swap! requires
         (fn [rmap]
           (if-not (contains? rmap namespace)
             (do-require namespace))
           (conj rmap namespace))))

(defn ins-init [namespace]
  [[] (atom {:namespace namespace})])

(defn ins-addDependency [group-id artifact-id version]
  (let [coords (->> (format "'[[%s/%s \"%s\"]]" group-id artifact-id version)
                    read-string
                    eval)]
    (log/infof "adding dependencies: %s" coords)
    (pom/add-dependencies :coordinates coords
                          :repositories @repositories)))

(defn ins-instance
  ([] (ins-instance nil))
  ([namespace]
   (let [namespace (or namespace "user")]
     (eval `(new com.zensols.py4j.InvokableNamespace ~namespace)))))

(defn ins-getNamespace [this]
  (-> (.state this) deref :namespace))

(defn ins-invoke [this function-name args]
  (let [namespace (ins-getNamespace this)]
    (log/infof "invoking: (%s/%s %s)" namespace function-name
               (str "<" (s/join "> <" args) ">"))
    (maybe-require namespace)
    (if-let [func (ns-resolve (symbol namespace) (symbol function-name))]
      (do (log/debugf "using function: %s" func)
          (apply func args))
      (-> (format "No such function: %s/%s" namespace function-name)
          (ex-info {:namespace namespace
                    :function function-name})
          throw))))

(defn ins-toString [this]
  (str "namespace=" (ins-getNamespace this)))

;; taken from clojure/clojure-contrib
(defmacro with-ns
  "Evaluates body in another namespace.  ns is either a namespace
  object or a symbol.  This makes it possible to define functions in
  namespaces other than the current one."
  [ns & body]
  `(binding [*ns* (the-ns ~ns)]
     ~@(map (fn [form] `(eval '~form)) body)))

(defn ins-eval
  "Eval Clojure **code** by binding **context** in a `let` and return results
  of all forms.

  The **context** parameter is an instance of a `java.util.Map`."
  ([this code] (ins-eval this code nil))
  ([this code context]
   (let [context (or context {})]
     (->> `(with-ns (quote ~(symbol (ins-getNamespace this)))
             ~(concat
               `(let ~(->> context
                           (mapcat (fn [[k v]]
                                     [(symbol (name k)) v]))
                           vec))
               `~(with-open [r (->> (java.io.StringReader. code)
                                    java.io.PushbackReader.)]
                   (->> (repeatedly #(read r false nil))
                        (take-while identity)
                        doall))))
          eval))))
