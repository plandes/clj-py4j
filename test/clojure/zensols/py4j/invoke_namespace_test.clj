(ns zensols.py4j.invoke-namespace-test
  (:import [com.zensols.py4j InvokableNamespace])
  (:require [clojure.test :refer :all]
            [zensols.py4j.invoke-namespace :refer :all]))

(clojure.string/join "," ["one" "two"])

(deftest test-function
  (testing "function"
    (let [args (into-array Object [" " ["one" "two"]])]
      (is (= "one two"
             (-> (InvokableNamespace. "clojure.string")
                 (.invoke "join" args)))))))

(deftest test-by-get-instance
  (testing "instance static method"
    (let [args (into-array Object [" " ["one" "two"]])]
      (is (= "one two"
             (-> (InvokableNamespace/instance "clojure.string")
                 (.invoke "join" args)))))))


;;; comment this out until figure out a way to run with DynamicClassloader
;;
;; (deftest test-deps
;;   (InvokableNamespace/addDependency "com.taoensso" "nippy" "2.13.0")
;;   (testing "dependency function"
;;     (let [item '[test "array" 123]
;;           func (InvokableNamespace. "taoensso.nippy")]
;;       (is (= item
;;              (let [bytes (.invoke func "freeze" (into-array [item]))]
;;                (eval '(do (require '[taoensso.nippy])))
;;                ((ns-resolve 'taoensso.nippy 'thaw) bytes)))))))
