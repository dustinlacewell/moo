#!/bin/sh

if [ ! $2 ] ; then
	echo "Syntax: $0 rc/rh target"
	exit 1
fi

if [ ! -d ~/.tinc/$1 ] ; then
	echo "Config path doesn't exist"
	exit 1
fi

if [ ! -f ~/.tinc/$1/tinc.conf ] ; then
	echo "Config file doesn't exist"
	exit 1
fi

sed -i '/^ConnectTo.*'$2'$/d' ~/.tinc/$1/tinc.conf
echo "Removed ConnecTo for $2 on layer $1"

