#!/bin/sh

if [ ! $6 ] ; then
	echo "Syntax: $0 database host port protocol user pass [group]"
	exit 1
fi

which sqlite3 1>/dev/null 2>&1
if [ $? -ne 0 ] ; then
	echo "SQLite not found"
	exit 1
fi

WORK="/tmp/.moo.work"
QUERY="DELETE FROM servercontrol WHERE \`host\` = '$2' AND \`port\` = '$3' AND \`protocol\` = '$4' AND \`user\` = '$5' AND \`pass\` = '$6' AND \`group\` = '$7';"
echo $QUERY > $WORK
sqlite3 $1 < $WORK
rm $WORK

