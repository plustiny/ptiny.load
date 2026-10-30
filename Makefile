# Makefile
gitupload:
	./gradlew clean
	git add .
	git commit -m "$${MSG:-$$(date -Is)}" || true
	git push -u origin main

-include .makefile.mk
.PHONY: gitupload
.PHONY: all clean
