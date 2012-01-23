#!/bin/bash

. ./build.sh

tar zcf moo.tar.gz moo.jar lib *.template run.sh servercontrol
rm -f moo.jar

