#! /usr/bin/env make -f
SUBDIRS = frontend backend backend-grpc backend-rest api-video-process

all: $(SUBDIRS)

run-devel:
	docker-compose -f docker-compose-devel.yml up -d

clear-devel:
	docker-compose -f docker-compose-devel.yml down --rmi all

containers: $(SUBDIRS)
	for i in $(SUBDIRS); do \
		$(MAKE) -C $$i $@; \
	done

frontend:
	$(MAKE) -C $@

backend:
	$(MAKE) -C $@

backend-grpc:
	$(MAKE) -C $@

backend-rest:
	$(MAKE) -C $@

api-video-process:
	$(MAKE) -C $@

clean:
	for i in $(SUBDIRS); do \
		$(MAKE) -C $$i $@; \
	done

.PHONY: all run-devel clear-devel containers frontend backend backend-grpc backend-rest api-video-process clean
