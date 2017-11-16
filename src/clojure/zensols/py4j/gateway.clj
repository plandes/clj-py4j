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
            ;; needed to define the InvokeableNamespace import
            [zensols.py4j.invoke-namespace :as in])
  ;; order matters
  (:import [com.zensols.py4j InvokableNamespace]))

(def default-port
  "The default gateway port from the API.

  Currently, this is `25333`."
  GatewayServer/DEFAULT_PORT)

(defonce ^:private server-inst (atom nil))

(defn invokable-namespace
  "Create a default invokable namespace."
  ([] (invokable-namespace (name in/invoke-default-namespace)))
  ([namespace]
   (InvokableNamespace/instance namespace)))

(defn gateway
  "Create and start a `py4j.GatewayServer`.

  If it's already been created and started, then return that instance."
  [& {:keys [port entry-point]
      :or {port default-port
           entry-point (invokable-namespace)}}]
  (locking server-inst
    (swap! server-inst
           #(or % (do (log/infof "starting gateway on port %s" port)
                      (doto (GatewayServer. entry-point port)
                        (.start)))))))

(defn- shutdown-gateway
  "Shutdown the give gateway immediately."
  [gw exit?]
  (log/info "shutting down gateway")
  (.shutdown gw)
  (when exit?
    (log/info "exiting JVM")
    (System/exit 0)))

(defn shutdown
  "Shutdown the gateway server.  If key `:timeout` is provided it will shutdown
  the gateway immediately if `:now`, otherwise it expects an integer in
  milliseconds in the future to shut it down. "
  [& {:keys [timeout exit?]
      :or {timeout :now
           exit? false}}]
  (locking server-inst
    (swap! server-inst
           (fn [gw]
             (when gw
               (log/infof "shutting down gateway with timeout: %s" timeout)
               (cond (= timeout :now) (shutdown-gateway gw exit?)
                     (integer? timeout) (future (do (Thread/sleep timeout)
                                                    (shutdown-gateway gw exit?)))
                     true (-> (format "Unknown shutdown timeout: %s" timeout)
                              (ex-info {:timeout timeout})
                              throw)))
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
    (repl/repl-port-set-option "-r" "--replport" nil)
    ["-t" "--timeout" "time to keep the gateway or forever if not given"
     :required "<milliseconds>"
     :default 0
     :parse-fn #(Integer/parseInt %)]]
   :app (fn [{:keys [port entry replport timeout] :as opts} & args]
          (if replport
            (repl/run-server replport))
          (if entry
            (gateway :port port :entry-point entry)
            (gateway :port port))
          (when (and timeout (> timeout 0))
            (future
              (Thread/sleep timeout)
              (log/infof "timeout expired--exiting")
              (System/exit 0))))})
