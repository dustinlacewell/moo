mkdir certs
mkdir private
mkdir pems
rm -f index.txt* serial crlnumber
touch index.txt
echo 01 > serial
echo 01 > crlnumber
