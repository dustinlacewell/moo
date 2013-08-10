#!/bin/bash
# upgrade.sh: Upgrade moo safely.

abort()
{
	echo "$@ failed, aborting..."
	exit 1
}

MOOPID=$(ps x | grep "java" | grep "moo" | awk '{ print $1 }')
if [ ! $MOOPID ]; then
	abort "Retrieving PID for running moo process"
fi

MOOHOME=""
PLANNED=0

# This is a very elaborate way of finding moo's directory.
# BSD needs procstat instead. Yay.
if [ ! -x "$(which pwdx)" ]; then
	echo 'Could not find the pwdx command; defaulting to ".." as the moo directory.'
	PLANNED=1
else
	if [ $(uname -s) = "FreeBSD" ]; then
		MOOHOME=$(procstat -f $MOOPID | grep "cwd" | awk '{ print $10 }')
	else
		MOOHOME=$(pwdx $MOOPID | awk '{ print $2 }')
	fi
fi

if [ ! $MOOHOME ]; then
	if [ $PLANNED -ne 1 ]; then
		echo 'Could not look up the moo directory; defaulting to "..".'
	fi
	MOOHOME=".."
fi

cd $MOOHOME

git pull
if [ $? -ne 0 ]; then
	abort "git pull"
fi

echo "Replaying log..."
git log ORIG_HEAD..
echo "Done"

sh build.sh
if [ $? -ne 0 ]; then
	abort "build.sh"
fi

# We can't kill the process because it's important that we send a proper QUIT
# message.
echo "!MOO!SHUTDOWN Upgrade in progress..."

# Give the process a couple of seconds to properly die and close connections.
sleep 10

exec ./run.sh

