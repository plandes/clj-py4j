(ns ^{:doc "Controller application to help with invoking Clojure funcrtions
from python.  This uses the [py4j](https://www.py4j.org) Java Gateway server
library unmarshall requests.  This namespace then services the request via
the [[zensols.py4j.invoke-namespace]] library."
      :author "Paul Landes"}
    zensols.py4j.gateway
  (:import [py4j GatewayServer])
  (:require [clojure.tools.logging :as log]
            [zensols.actioncli.log4j2 :as lu]
            [zensols.actioncli.repl :as repl]
            [zensols.py4j.invoke-namespace :as f])
  ;; order matters
  (:import [com.zensols.py4j InvokableNamespace]))

(def default-port
  "The default gateway port from the API.

  Currently, this is `25333`."
  GatewayServer/DEFAULT_PORT)

(defonce ^:private server-inst (atom nil))

(defn invokable-namespace
  "Create a default invokable namespace."
  ([] (invokable-namespace "zensols.py4j.gateway"))
  ([namespace]
   (InvokableNamespace/instance namespace)))

(defn gateway
  "Create and start a `py4j.GatewayServer`.

  If it's already been created and started, then return that instance."
  [& {:keys [port entry-point]
      :or {port default-port
           entry-point (invokable-namespace)}}]
  (locking server-inst
    (swap! server-inst #(or % (doto (GatewayServer. entry-point port)
                                (.start))))))

(defn shutdown
  "Shutdown the gateway server."
  []
  (locking server-inst
    (swap! server-inst (fn [gw]
                         (and gw (.shutdown gw))
                         nil))))

(defn restart
  "Restart the gateway server."
  []
  (shutdown)
  (gateway))

(defn gateway-port-set-option
  ([]
   (gateway-port-set-option "-p" "--port"))
  ([short long]
   (gateway-port-set-option short long default-port))
  ([short long port]
   [short long "the port bind for the gateway server"
    :required "<number>"
    :default port
    :parse-fn #(Integer/parseInt %)
    :validate [#(< 0 % 0x10000) "Must be a number between 0 and 65536"]]))

(def gateway-command
  "CLI command to start the py4j Java side gateway"
  {:description "start the py4j Java side gateway"
   :options
   [(lu/log-level-set-option)
    (gateway-port-set-option)
    ["-e" "--entry" "use the option given as the entry point for the gateway"
     :required "<string>"]
    (repl/repl-port-set-option "-r" "--replport" nil)]
   :app (fn [{:keys [port entry replport] :as opts} & args]
          (if replport
            (repl/run-server replport))
          (if entry
            (gateway :port port :entry-point entry)
            (gateway :port port)))})
