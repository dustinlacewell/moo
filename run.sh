#!/bin/bash

mvn exec:exec -pl moo

#if grep -q -E '^debug: false$' moo.yml ; then
#	java $ARGS -jar $JAR >moo.log 2>&1 &
#	echo "moo started"
#else
#	java $ARGS -jar $JAR
#fi

