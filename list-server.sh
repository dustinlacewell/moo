#!/bin/sh

if [ ! $1 ] ; then
	echo "Syntax: $0 database"
	exit 1
fi

which sqlite3 1>/dev/null 2>&1
if [ $? -ne 0 ] ; then
	echo "SQLite not found"
	exit 1
fi

WORK="/tmp/.moo.work"
QUERY="SELECT \`host\`,\`port\`,\`protocol\`,\`user\`,\`pass\`,\`group\` FROM servercontrol;"
echo $QUERY > $WORK
echo "Host Port Protocol User Pass Group"
echo "----------------------------------"
sqlite3 $1 < $WORK | sed  's/|/ /g'
rm $WORK

