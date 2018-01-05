#!/bin/sh

if [ -z "$1" ]
  then
    echo "No argument supplied"
    exit 1
fi
SITE=$1

echo Create key for: $SITE

rm -f keys/$SITE/site.private
rm -f keys/$SITE/site.csr
rm -f keys/$SITE/site.cert
rm -f keys/$SITE/site.p12

mkdir -p keys/$SITE

echo Generate private key for site...
openssl genrsa -out keys/$SITE/site.private 2048

sed "s/serverDomainName/$SITE/g" template.cert.config >keys/$SITE/site.cert.config

echo Create a certificate request for the site described in the configuration...
openssl req -new -key keys/$SITE/site.private -out keys/$SITE/site.csr -config keys/$SITE/site.cert.config

echo Sign the certificate with the CA private key...
openssl x509 -in keys/$SITE/site.csr -out keys/$SITE/site.cert -req -CA keys/repocache.qgears.com.cert -CAkey keys/repocache.qgears.com.private -CAcreateserial -days 365

echo MD5sum of the private key modulus...
openssl rsa -noout -modulus -in keys/$SITE/site.private |openssl md5

echo MD5sum of the root CA private key modulus...
openssl rsa -noout -modulus -in keys/repocache.qgears.com.private |openssl md5

echo MD5Sum of csr request
openssl req -noout -modulus -in keys/$SITE/site.csr |openssl md5

echo MD5sum of the cert private key modulus... \(should be equal to previous\)
openssl x509 -noout -modulus -in keys/$SITE/site.cert | openssl md5


echo pack cert and key into a pkcs12 format file...
openssl pkcs12 -export -nodes -in keys/$SITE/site.cert -inkey keys/$SITE/site.private -out keys/$SITE/site.p12 -name site -CAfile keys/repocache.qgears.com.cert -caname root -passout pass:verysec

