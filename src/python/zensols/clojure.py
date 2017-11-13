from py4j.java_gateway import JavaGateway, GatewayParameters
import logging

logger = logging.getLogger('py4j.clojure')

class ClojureWrapper(object):
    """
    Invoke Clojure call via a py4j gateway.

usage:

  ClojureWrapper.call("clojure.string", "join", "||", ["test", "one", 2234])
    """
    def __init__(self, namespace=None, address='127.0.0.1', port=25333):
        self.params = GatewayParameters(address=address, port=port)
        self.ns_name = namespace
        self.ns_obj = None
        self.gateway = None

    def _java_object(self, o):
        """Convert **o** to a Java object usable by the gateway."""
        if isinstance(o, list) or isinstance(o, tuple):
            return self._java_array(o)
        return o

    def _java_array(self, args):
        """Convert args from a python list to a Java array."""
        alen = len(args)
        jargs = self._gw.new_array(self._jvm.Object, alen)
        for i in range(alen):
            jargs[i] = self._java_object(args[i])
        return jargs

    @property
    def _gw(self):
        """Return the gateway"""
        if self.gateway == None:
            logger.info('connecting to %s' % self.params)
            self.gateway = JavaGateway(gateway_parameters=self.params)
        return self.gateway

    @property
    def _jvm(self):
        """Return the gateway's JVM"""
        return self._gw.jvm

    def set_namespace(self, namespace):
        """Set the Clojure namespace"""
        self.ns_name = namespace
        self.ns_obj = None

    def _new_namespace(self, namespace):
        """Create a new `com.zensols.py4j.InvokableNamespace` instance."""
        logger.debug('creating namespace: %s' % namespace)
        return self._jvm.com.zensols.py4j.InvokableNamespace.instance(namespace)

    def get_namespace(self):
        """Return a `com.zensols.py4j.InvokableNamespace` instance."""
        if self.ns_obj == None:
            self.ns_obj = self._new_namespace(self.ns_name)
        return self.ns_obj

    def invoke(self, function_name, *args):
        """Invoke a function in the namespace."""
        jargs = self._java_array(args)
        return self.get_namespace().invoke(function_name, jargs)

    def close(self):
        """Close the gateway."""
        logger.info('shutting down')
        if self.gateway != None:
            self.gateway.close()
            self.gateway = None

    def shutdown(self):
        """Shutdown the py4j gateway server (careful!)."""
        self._gw.shutdown()
        self.close()

    def add_depenedency(self, group_id, artifact_id, version):
        """Download and classload a maven coordinate."""
        self._jvm.com.zensols.py4j.InvokableNamespace.addDependency(group_id, artifact_id, version)

    @property
    def entry_point(self):
        """Return the gateway entry point."""
        return self._gw.entry_point

    @classmethod
    def call(cls, namespace, function_name, *args):
        """One shot convenience method to that invokes a function."""
        inst = ClojureWrapper(namespace)
        try:
            return inst.invoke(function_name, *args)
        finally:
            inst.close()

    @classmethod
    def kill_server(cls):
        """Stop the Clojure p4j gateway server (careful!)."""
        ClojureWrapper.call('zensols.py4j.gateway', 'shutdown')
