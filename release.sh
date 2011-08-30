#!/bin/bash

. ./build.sh

tar zcf moo.tar.gz moo.jar *.template run.sh
rm -f moo.jar

