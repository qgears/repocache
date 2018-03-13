#!/bin/sh
FOLDER=keys
PUBLICFOLDER=public
FNAME=${1:-repocache.qgears.com}
FILE=$FOLDER/$FNAME
mkdir -p $FOLDER
mkdir -p $PUBLICFOLDER

# Generate private key
echo genrsa
openssl genrsa -out $FILE.private 2048

# Request a CA cert
echo req
openssl req -new -key $FILE.private -out $FILE.csr -config cert-config.txt

# Sign the CA cert with the private key: the signed cert is the root cert
echo x509
openssl x509 -req -days 3652 -in $FILE.csr -signkey $FILE.private -out $PUBLICFOLDER/$FNAME.crt



