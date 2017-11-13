(ns zensols.py4j.core
  (:require [zensols.actioncli.log4j2 :as lu]
            [zensols.actioncli.parse :as cli])
  (:require [zensols.py4j.version :as ver])
  (:gen-class :main true))

(defn- version-info []
  (println (format "%s (%s)" ver/version ver/gitref)))

(defn- create-action-context []
  (cli/single-action-context
   '(zensols.py4j.gateway gateway-command)
   :version-option (cli/version-option version-info)))

(defn -main [& args]
  (lu/configure "gateway-log4j2.xml")
  (cli/set-program-name "gateway")
  (-> (create-action-context)
      (cli/process-arguments args)))

(defn main [args]
  (apply -main args))
