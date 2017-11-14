# Python to Clojure Bridge

[![Travis CI Build Status][travis-badge]][travis-link]

Python to Clojure Bridge using a Py4J Gateway.  This simple library aims to
make it easy and trivial to invoke [Clojure] from [Python] using the [py4j]
gateway server and API.

If you think this sounds convoluted, it is.  However it also solves a lot of
issues using a JVM API and proxying in the same process.  This method separates
the JVM and python in separate processes so they don't step on eachother's feet
(think memory and resource allocation issues).  On the downside the setup is
more complex.

The end to end request looks like:

1. Invoke the [clojure Python library](python/clojure/api.py).
2. Marshall the RPC request via the py4j python library
3. Request is sent via the network
4. Request received py4j Java Gateway server
5. `zensols.py4j.gateway` (this library) invokes the actual Clojure request
6. Return the result back all the way up the chain.


<!-- markdown-toc start - Don't edit this section. Run M-x markdown-toc-refresh-toc -->
## Table of Contents

- [Usage](#usage)
    - [NLP Complex Example](#nlp-complex-example)
    - [Installing and Running](#installing-and-running)
- [Obtaining](#obtaining)
- [Documentation](#documentation)
- [Binaries](#binaries)
- [Building](#building)
- [Changelog](#changelog)
- [License](#license)

<!-- markdown-toc end -->


## Usage

First [download, install and run the server](#installing-and-running)

```python
from zensols.clojure import Clojure

def test():
    cw = Clojure('taoensso.nippy')
    try:
        cw.add_depenedency('com.taoensso', 'nippy', '2.13.0')
        dat = cw.invoke('freeze', [123, 'strarg', 1.2])
        thawed = cw.invoke('thaw', dat)
        for i in thawed:
            print('thawed item: %s' % i)
    finally:
        cw.close()

>>> test()
>>> thawed item: 123
thawed item: strarg
thawed item: 1.2
```

See the [test cases](test/python/tests.py) for more examples.


### NLP Complex Example

This example uses the [NLP Clojure Project] parse function.  The `py4jgw`
(gateway) needs
the [models installed](https://github.com/plandes/clj-nlp-parse#setup) and the
following system property set by adding it to the environment setup script:

```bash
$ echo 'JAVA_OPTS="-Dzensols.model=${HOME}/opt/nlp/model"' > py4jgw/bin/setupenv
$ /bin/bash py4jgw/bin/py4jgw
```

The following example parses an utterance and prints out what could be used as
features in a [machine learning](https://github.com/plandes/clj-ml-model)
model:

```python
import json
from zensols.clojure import Clojure

def test():
    parse = Clojure('zensols.nlparse.parse')
    cjson = Clojure('clojure.data.json')
    try:
        parse.add_depenedency('com.zensols.nlp', 'parse', '0.1.4')
        cjson.add_depenedency('org.clojure', 'data.json', '0.2.6')
        panon = parse.invoke('parse', """I LOVE Bill Joy and he's the smartest guy in the world!""")
        jstr = cjson.invoke('write-str', panon)
        parsed = json.loads(jstr)
        print('sentiment: %s' % parsed['sentiment'])
        ment = parsed['mentions'][0]
        print("'%s' is a %s" % (ment['text'], ment['ner-tag']))
        #print(json.dumps(parsed, indent=2, sort_keys=True))
    finally:
        parse.close()
        cjson.close()

>>> test()
>>> sentiment: 1
'Bill Joy' is a PERSON
{
  "mentions": [
    {
      "char-range": [
        7, 
        15
      ], 
	  ...
```


### Installing and Running

1. Download the binary:
   `$ wget https://github.com/plandes/clj-py4j/releases/download/v0.0.1/py4jgw.zip`
2. Extract: `$ unzip py4jgw.zip`
3. Run the server: `$ /bin/bash ./py4jgw/bin/py4jgw` (or `py4jgw\bin\py4jgw.bat`)
4. Install the Python library: `$ pip install zensols.clojure`
5. [Hack!](#usage)


## Obtaining

In your `project.clj` file, add:

[![Clojars Project](https://clojars.org/com.zensols.py4j/gateway/latest-version.svg)](https://clojars.org/com.zensols.py4j/gateway/)

### Binaries

The latest release binaries are
available [here](https://github.com/plandes/clj-py4j/releases/latest).


## Documentation

* [Clojure](https://plandes.github.io/clj-py4j/codox/index.html)
* [Java](https://plandes.github.io/clj-py4j/apidocs/index.html)


## Building

To build from source, do the folling:

- Install [Leiningen](http://leiningen.org) (this is just a script)
- Install [GNU make](https://www.gnu.org/software/make/)
- Install [Git](https://git-scm.com)
- Download the source: `git clone https://github.com/plandes/clj-py4j && cd clj-py4j`
- Download the make include files:
```bash
mkdir ../clj-zenbuild && wget -O - https://api.github.com/repos/plandes/clj-zenbuild/tarball | tar zxfv - -C ../clj-zenbuild --strip-components 1
```
- Build the software: `make jar`
- Build the distribution binaries: `make dist`
- Build the Python egg/wheel distribution libraries: `make pydist`

Note that you can also build a single jar file with all the dependencies with: `make uber`


## Changelog

An extensive changelog is available [here](CHANGELOG.md).


## License

Copyright © 2017 Paul Landes

Apache License version 2.0

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.


<!-- links-->

[NLP Clojure Project]: https://github.com/plandes/clj-nlp-parse
[py4j]: https://www.py4j.org
[Clojure]: https://clojure.org
[Python]: https://www.python.org
[travis-link]: https://travis-ci.org/plandes/clj-py4j
[travis-badge]: https://travis-ci.org/plandes/clj-py4j.svg?branch=master
