openssl req -new -key ircd/keys/$1.key -nodes -keyform PEM -out ircd/keys/$1.csr -subj "/L=Rizon/CN=irc.rizon.net/O=Rizon IRC Network/OU=$1/emailAddress=routing@rizon.net"
