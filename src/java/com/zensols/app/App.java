package com.zensols.app;

import org.projectodd.shimdandy.ClojureRuntimeShim;

/**
 * Stand along app needs to be called with a <em>modifyable</em> parent
 * classloader.
 *
 * @see <a href="https://github.com/cemerick/pomegranate#urlclassloader-modifiability">URLClassLoader modifiability issue</a>
 * @author Paul Landes
 */
public class App {
    public static void main(String[] args) throws Exception {
        ClassLoader cl = new clojure.lang.DynamicClassLoader(App.class.getClassLoader());
        ClojureRuntimeShim rt = ClojureRuntimeShim.newRuntime(cl);
	rt.require("zensols.py4j.core");
	try {
	    rt.invoke("zensols.py4j.core/main", args);
	} finally {
	    try { rt.close(); }
	    catch (Exception e) { e.printStackTrace(); }
	}
    }
}
