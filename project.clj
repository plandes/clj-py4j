(defproject com.zensols.py4j/gateway "0.1.0-SNAPSHOT"
  :description "Python to Clojure Bridge using a Py4J Gateway"
  :url "https://github.com/plandes/clj-py4j"
  :license {:name "Apache License version 2.0"
            :url "https://www.apache.org/licenses/LICENSE-2.0"
            :distribution :repo}
  :plugins [[lein-codox "0.10.3"]
            [lein-javadoc "0.3.0"]
            [org.clojars.cvillecsteele/lein-git-version "1.2.7"]]
  :codox {:metadata {:doc/format :markdown}
          :project {:name "Python to Clojure Bridge"}
          :output-path "target/doc/codox"
          :source-uri "https://github.com/plandes/clj-py4j/blob/v{version}/{filepath}#L{line}"}
  :javadoc-opts {:package-names ["com.zensols.app"]
                 :output-dir "target/doc/apidocs"}
  :git-version {:root-ns "zensols.py4j"
                :path "src/clojure/zensols/py4j"
                :version-cmd "git describe --match v*.* --abbrev=4 --dirty=-dirty"}
  :source-paths ["src/clojure"]
  :test-paths ["test/clojure" "test-resources"]
  :java-source-paths ["src/java"]
  :javac-options ["-Xlint:unchecked"]
  :jar-exclusions [#".gitignore"]
  :dependencies [[org.clojure/clojure "1.8.0"]

                 ;; logging for core
                 [org.apache.logging.log4j/log4j-core "2.7"]
                 [org.apache.logging.log4j/log4j-slf4j-impl "2.7"]
                 [org.apache.logging.log4j/log4j-1.2-api "2.7"]
                 [org.apache.logging.log4j/log4j-jcl "2.7"]
                 [org.apache.logging.log4j/log4j-jul "2.7"]

                 ;; command line
                 [com.zensols.tools/actioncli "0.0.22"]

                 ;; dependency (shimdaddy needed for Java 9 classloader)
                 [org.projectodd.shimdandy/shimdandy-api "1.2.0"]
                 [org.projectodd.shimdandy/shimdandy-impl "1.2.0"]
                 [com.cemerick/pomegranate "1.0.0"]

                 ;; py4j gateway server
                 [net.sf.py4j/py4j "0.10.6"]]
  :pom-plugins [[org.codehaus.mojo/appassembler-maven-plugin "1.6"
                 {:configuration ([:programs
                                   [:program
                                    ([:mainClass "com.zensols.app.App"]
                                     [:id "py4jgw"])]]
                                  [:environmentSetupFileName "setupenv"])}]]
  :profiles {:uberjar {:aot [zensols.py4j.core]}
             :appassem {:aot :all}
             :snapshot {:git-version {:version-cmd "echo -snapshot"}}
             :runserv {:main com.zensols.app.App}
             :test {:jvm-opts ["-Dlog4j.configurationFile=test-resources/test-log4j2.xml"
                               "-Xms4g" "-Xmx12g" "-XX:+UseConcMarkSweepGC"]}}
  :main zensols.py4j.core
  :aot [zensols.py4j.invoke-namespace])
