#!/bin/bash
BOLD=$(tput bold)
NORM=$(tput sgr0)

echo pdns0 running on irc.cccp-project.net
dig soa irc.rizon.net @pdns0.rizon.net | grep "SOA"
echo pdns1 running on irc.cyberdynesystems.net
dig soa irc.rizon.net @pdns1.rizon.net | grep "SOA"
echo pdns2 running on irc.shakeababy.net
dig soa irc.rizon.net @pdns2.rizon.net | grep "SOA"
echo pdns3 running on irc.x2x.cc
dig soa irc.rizon.net @pdns3.rizon.net | grep "SOA"

