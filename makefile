## makefile automates the build and deployment for lein projects

# location of the http://github.com/plandes/clj-zenbuild cloned directory
ZBHOME ?=	../clj-zenbuild
ANRRES=		py4jgw
REL_DIST ?=	$(REL_ZIP) $(MTARG_PYDIST_ATFC)

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
	lein with-profile +runserv run -t 10000 &
	@for i in `seq 1 10` ; do \
		echo attempt gateway connection $$i ; \
		nc -d -w 0 localhost 25333 && break ; \
	done
	make pytest

.PHONY:	pinst
pinst:
	yes | pip uninstall clojure || true
	make pyinstall
