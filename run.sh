#!/bin/bash

java -XX:+HeapDumpOnOutOfMemoryError -Xmx16M -jar moo.jar >moo.log 2>&1 &
