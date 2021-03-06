## makefile automates the build and deployment for lein projects

PROJ_TYPE=		clojure
PROJ_MODULES=		dist python release
ANRRES=			py4jgw
REL_DIST ?=		$(REL_ZIP) $(MTARG_PYDIST_ATFC)

include $(if $(ZBHOME),$(ZBHOME),../zenbuild)/main.mk

.PHONY:	runserv
runserv:
	lein with-profile +runserv run || true

.PHONY: test
test:
	$(LEIN) test

# integration tests output errors (specifically when the java server exists)
.PHONY:	inttest
inttest:
	lein with-profile +runserv run -t 15000 &
	@for i in `seq 1 30` ; do \
		echo attempt gateway connection $$i ; \
		nc -d -w 0 localhost 25333 && break ; \
		sleep 1 ; \
	done
	make pytest

.PHONY:	pinst
pinst:
	yes | pip uninstall clojure || true
	make pyinstall

.PHNOY:	pydebugtest
pydebugtest:
	make TEST_DEBUG=1 pytest
