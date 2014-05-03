#!/bin/bash

JAR=target/moo-2.0-SNAPSHOT-jar-with-dependencies.jar

if [ `grep ^debug=0$ moo.properties` ] ; then
	java -XX:+HeapDumpOnOutOfMemoryError -Xmx16M -jar $JAR >moo.log 2>&1 &
	echo "moo started"
else
	java -XX:+HeapDumpOnOutOfMemoryError -Xmx16M -jar $JAR
fi

