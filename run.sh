#!/bin/bash

JAR=moo/target/moo-moo-3.0-SNAPSHOT-jar-with-dependencies.jar
ARGS='-XX:+HeapDumpOnOutOfMemoryError -Xmx16M'

if grep -q -E '^debug: false$' moo.yml ; then
	java $ARGS -jar $JAR >moo.log 2>&1 &
	echo "moo started"
else
	java $ARGS -jar $JAR
fi

