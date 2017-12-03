#!/usr/bin/env python

import py4j
import unittest, logging, sys, os
from zensols.clojure import Clojure

logger = logging.getLogger('py4j.clojure.test')

class TestClojure(unittest.TestCase):
    def test_call(self):
        self.assertEqual('test||one||2234', Clojure.call('clojure.string', 'join', '||', ['test', 'one', 2234]))

    def test_nippy(self):
        cw = Clojure('taoensso.nippy')
        try:
            cw.add_depenedency('com.taoensso', 'nippy', '2.13.0')
            dat = cw.invoke('freeze', [123, 'strarg', 1.2])
            thawed = cw.invoke('thaw', dat)
            self.assertEqual(123, thawed[0])
            self.assertEqual('strarg', thawed[1])
            self.assertEqual(1.2, thawed[2])
        finally:
            cw.close()

    def test_entrypoint(self):
        cw = Clojure()
        try:
            ep = cw.entry_point
            logger.info('entry point: %s' % ep)
            eps = ep.toString()
            self.assertGreater(len(eps), 0)
        finally:
            cw.close()

    def test_eval(self):
        cw = Clojure()
        try:
            ret = cw.eval('(+ 1 1 a b)', {'a': 5.5, 'b': 8})
            self.assertEqual(ret, 15.5)
        finally:
            cw.close()

    def test_other_namespace(self):
        cw = Clojure('clojure.string')
        try:
            ret = cw.eval("""
(join "," ["one" "two" "three"])
""")
            self.assertEqual(ret, "one,two,three")
        finally:
            cw.close()

    def test_namespace(self):
        cw = Clojure()
        try:
            ret = cw.eval("""
(with-ns 'clojure.string
  (join "," ["one" "two" "three"]))
""")
            self.assertEqual(ret, "one,two,three")
        finally:
            cw.close()

    def test_temp_namespace(self):
        cw = Clojure()
        try:
            ret = cw.eval("""
(with-temp-ns 'some-new-ns
  (def never-seen 2)
  (require '[clojure.string :as s])
  (s/join "," ["one" "two" "three" never-seen]))
""")
            self.assertEqual(ret, "one,two,three,2")
        finally:
            cw.close()

    def test_entrypoint_ns(self):
        cw = Clojure()
        try:
            ep = cw.entry_point
            self.assertEqual(ep.eval("""
(with-ns 'clojure.string
  (capitalize "stuff"))
"""), "Stuff")
        finally:
            cw.close()

    def log_val(self, v):
        logger.info('aval: %s (%s)' % (v, type(v)))

    def test_python_object(self):
        cw = Clojure()
        try:
            o = cw.eval("""{:a 1 :b 2 :arr [{:animal "dog"} 99.2 :kw]}""")
            o = cw.python_object(o)
            self.assertTrue(isinstance(o, dict))
            aval = o['a']
            self.log_val(aval)
            self.assertTrue(isinstance(aval, int) or isinstance(aval, long))
            self.assertEqual(aval, 1)
            arr = o['arr']
            self.assertTrue(isinstance(arr, list))
            arrmap = arr[0]
            self.assertTrue(isinstance(arrmap, dict))
            mval = arrmap['animal']
            self.log_val(mval)
            self.assertTrue(isinstance(mval, str) or isinstance(mval, unicode))
            self.assertEqual('dog', mval)
            self.log_val(arr[1])
            self.assertEqual(arr[1], 99.2)
            self.log_val(arr[2])
            self.assertTrue(isinstance(arr[2], str) or isinstance(arr[2], unicode))
            self.assertEqual(arr[2], ':kw')
        finally:
            cw.close()

    def test_embedded_python_object(self):
        cw = Clojure()
        try:
            o = cw.eval("""{:last :map}""")
            o = cw.eval("""{:a 1 :b 2 :arr [{:animal "dog"} 99.2 :kw lm]}""", {'lm': o})
            o = cw.python_object(o)
            self.assertEqual(o['arr'][3]['last'], ':map')
        finally:
            cw.close()


def enable_debug():
    logging.basicConfig(level=logging.WARN)
    if 'TEST_DEBUG' in os.environ and os.environ['TEST_DEBUG']:
        logger.setLevel(logging.DEBUG)

def main(args=sys.argv[1:]):
    enable_debug()
    if len(args) > 0 and args[0] == 'kill':
        print('shutting down...')
        Clojure.kill_server()
    else:
        unittest.main()

if __name__ == '__main__':
    main()
