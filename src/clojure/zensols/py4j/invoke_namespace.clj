(ns ^{:doc "Compiled into a Java class to help invoke Clojure functions."
      :author "Paul Landes"}
    zensols.py4j.invoke-namespace
  (:require [clojure.tools.logging :as log]
            [clojure.string :as s]
            [cemerick.pomegranate :as pom])
  (:gen-class
   :name "com.zensols.py4j.InvokableNamespace"
   :init init
   :state state
   :constructors {[Object] []}
   :methods [#^{:static true} [addDependency [String String String] void]
             #^{:static true} [instance [String] Object]
             [invoke [String "[Ljava.lang.Object;"] Object]]
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

(defn ins-instance [namespace]
  (eval `(new com.zensols.py4j.InvokableNamespace ~namespace)))

(defn- ins-getNamespace [this]
  (-> (.state this) deref :namespace))

(defn ins-invoke [this function-name args]
  (let [namespace (ins-getNamespace this)]
    (log/infof "invoking: (%s/%s %s)" namespace function-name
               (str "<" (s/join "> <" args) ">"))
    (maybe-require namespace)
    (let [func (ns-resolve (symbol namespace) (symbol function-name))]
      (log/debugf "using function: %s" func)
      (apply func args))))

(defn ins-toString [this]
  (str "namespace=" (ins-getNamespace this)))
