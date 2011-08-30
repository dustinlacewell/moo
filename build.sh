#!/bin/bash

ant

pushd bin
jar -mcf ../MANIFEST.MF moo.jar net/*
mv moo.jar ..
popd

rm -rf bin

