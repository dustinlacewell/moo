openssl req -config openssl.cnf -newkey rsa:4096 -days 3650 -x509 -keyout private/cakey.pem -out cacert.pem -nodes
