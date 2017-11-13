#!/usr/bin/env python

import unittest, logging, sys
from zensols.clojure import ClojureWrapper

logger = logging.getLogger('py4j.clojure')

class TestClojureWrapper(unittest.TestCase):
    def test_call(self):
        self.assertEqual('test||one||2234', ClojureWrapper.call('clojure.string', 'join', '||', ['test', 'one', 2234]))

    def test_nippy(self):
        cw = ClojureWrapper('taoensso.nippy')
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
        cw = ClojureWrapper()
        try:
            ep = cw.entry_point
            logger.info('entry point: %s' % ep)
            eps = ep.toString()
            self.assertGreater(len(eps), 0)
        finally:
            cw.close()

def enable_debug():
    logging.basicConfig(level=logging.WARN)
    logger.setLevel(logging.INFO)

def main(args=sys.argv[1:]):
    #enable_debug()
    if len(args) > 0 and args[0] == 'kill':
        print('shutting down...')
        ClojureWrapper.kill_server()
    else:
        unittest.main()

if __name__ == '__main__':
    main()
