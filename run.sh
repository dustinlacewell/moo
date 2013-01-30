#!/bin/bash

if [ `grep ^debug=0$ moo.properties` ] ; then
	java -XX:+HeapDumpOnOutOfMemoryError -Xmx16M -jar moo.jar >moo.log 2>&1 &
	echo "moo started"
else
	java -XX:+HeapDumpOnOutOfMemoryError -Xmx16M -jar moo.jar
fi

