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

if [ $4 != "ssh" ] ; then
	echo "Unsupported protocol"
	exit 1
fi

SCHEMA="CREATE TABLE IF NOT EXISTS servercontrol (\`host\` varchar(64), \`port\` int(11), \`protocol\` varchar(64), \`user\` varchar(64), \`pass\` varchar(64), \`group\` varchar(64));"
WORK="/tmp/.moo.work"

echo $SCHEMA > $WORK
sqlite3 $1 < $WORK
rm $WORK

QUERY="INSERT INTO servercontrol (\`host\`, \`port\`, \`protocol\`, \`user\`, \`pass\`, \`group\`) VALUES ('$2', '$3', '$4', '$5', '$6', '$7');"
echo $QUERY > $WORK
sqlite3 $1 < $WORK
rm $WORK

