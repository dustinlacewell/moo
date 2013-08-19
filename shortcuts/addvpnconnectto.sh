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

if [ "`grep ConnectTo.*$2 ~/.tinc/$1/tinc.conf`" ] ; then
	echo "ConnectTo already exists for $2 on layer $1"
	exit 1
fi

cat ~/.tinc/$1/tinc.conf | awk '/^ConnectTo/ && !modif { printf("ConnectTo	'$2'\n"); modif=1 } {print}' > tinc.conf.work
if [ $? -ne 0 ] ; then
	echo "Non 0 exit code $? from cat/awk, aborting"
	rm -f tinc.conf.work
	exit 1
fi

mv tinc.conf.work ~/.tinc/$1/tinc.conf
echo "Added ConnecTo for $2 on layer $1"

