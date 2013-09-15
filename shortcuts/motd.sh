#!/bin/bash

MOTD_FILE=~/ircd/etc/ircd.motd

if [ ! $1 ] ; then
        echo "Syntax: $0 server.name"
        exit 1
fi

cat > $MOTD_FILE << 'EOF'
         11oo                            

88d888b. dP d888888b .d8888b. 88d888b. 
88'  `88 88    .d8P' 88'  `88 88'  `88 
1188       88  .Y8P    88.  .88 88    88 
11dP       dP d888888P `88888P' dP    dP

Rizon Chat Network -- http://rizon.net

Listening on ports: 6660 - 6669, 7000. SSL: 6697, 9999

Rules:
o No spamming or flooding
o No clones or malicious bots
o No takeovers
o No distribution of child pornography
o Clients must respond to VERSION requests
o Rizon staff may disconnect clients for any or no reason

First steps:
o To register your nick: /msg NickServ HELP
o To register your channel: /msg ChanServ HELP
o To get a vHost: /msg HostServ HELP REQUEST
o For other help with Rizon: /join #help

Usage of this network is a privilege, not a right. Rizon is a
transit provider, therefore no person or entity involved with
*.rizon.net or $1 takes any responsibility for
users' actions. Absolutely no warranty is expressed or implied.
EOF

sed -i "s/\$1/$1/g" $MOTD_FILE

echo "Done, rehashing IRCd..."

pkill -USR1 ircd
