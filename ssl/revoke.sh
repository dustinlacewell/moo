openssl ca -config openssl.cnf -keyfile private/cakey.pem -cert cacert.pem -revoke pems/$1.pem

openssl ca -config openssl.cnf -keyfile private/cakey.pem -cert cacert.pem -gencrl -out crl.pem

