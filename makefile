## makefile automates the build and deployment for lein projects

# location of the http://github.com/plandes/clj-zenbuild cloned directory
ZBHOME ?=	../clj-zenbuild

#APP_NAME=	py4jgw
ANRRES=		py4jgw

REL_DIST ?=	$(REL_ZIP)

all:		info

include $(ZBHOME)/src/mk/compile.mk
include $(ZBHOME)/src/mk/dist.mk
include $(ZBHOME)/src/mk/python.mk
include $(ZBHOME)/src/mk/release.mk

.PHONY:	runserv
runserv:
	lein with-profile +runserv run || true

.PHONY: test
test:
	$(LEIN) test

# integration tests output errors (specifically when the java server exists)
.PHONY:	inttest
inttest:
	make runserv &
	sleep 10
	make pytest
	PYTHONPATH=$(PY_SRC) python $(PY_SRC_TEST)/* kill 2>/dev/null || true

.PHONY:	pinst
pinst:
	yes | pip uninstall clojure || true
	make pyinstall
