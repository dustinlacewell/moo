#!/bin/bash


if grep -q -E '^debug: false$' moo.yml ; then
	mvn exec:exec -pl moo -Dexec.executable=moo/src/main/resources/run.sh
else
	mvn exec:exec -pl moo -Dexec.executable=java
fi

