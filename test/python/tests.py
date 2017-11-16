#!/usr/bin/env python

import unittest, logging, sys
from zensols.clojure import Clojure

logger = logging.getLogger('py4j.clojure')

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

def enable_debug():
    logging.basicConfig(level=logging.WARN)
    logger.setLevel(logging.INFO)

def main(args=sys.argv[1:]):
    #enable_debug()
    if len(args) > 0 and args[0] == 'kill':
        print('shutting down...')
        Clojure.kill_server()
    else:
        unittest.main()

if __name__ == '__main__':
    main()
