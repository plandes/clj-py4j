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

(deftest test-eval
  (testing "evaulation"
    (is (= "a: 123\nb: 1.2\n"
           (with-out-str
             (-> (InvokableNamespace/instance)
                 (.eval "(println \"a:\" a) (println \"b:\" b)"
                        {"a" 123 "b" 1.2})))))))

(deftest test-eval-ns
  (testing "test def in ns"
    (is (= 'asym4
           (do (-> (InvokableNamespace/instance)
                   (.eval "(def testvar2 'asym4)" nil))
               (eval 'zensols.py4j.ns/testvar2))))))
