#!/bin/sh

rm -f keys/ssh-example.private
rm -f keys/ssh-example.csr
rm -f keys/ssh-example.cert
rm -f keys/ssh-example.jks
rm -f keys/ssh-example.p12


echo Generate private key for site...
openssl genrsa -out keys/ssh-example.private 2048

echo Create a certificate request for the site described in the configuration...
openssl req -new -key keys/ssh-example.private -out keys/ssh-example.csr -config ssh-example.cert.config


echo Sign the certificate with the CA private key...
openssl x509 -in keys/ssh-example.csr -out keys/ssh-example.cert -req -CA keys/repocache.qgears.com.cert -CAkey keys/repocache.qgears.com.private -CAcreateserial -days 365


echo MD5sum of the private key modulus...
openssl rsa -noout -modulus -in keys/ssh-example.private |openssl md5

echo MD5sum of the root CA private key modulus...
openssl rsa -noout -modulus -in keys/repocache.qgears.com.private |openssl md5

echo MD5Sum of csr request
openssl req -noout -modulus -in keys/ssh-example.csr |openssl md5

echo MD5sum of the cert private key modulus... \(should be equal to previous\)
openssl x509 -noout -modulus -in keys/ssh-example.cert | openssl md5


echo pack cert and key into a pkcs12 format file...
openssl pkcs12 -export -nodes -in keys/ssh-example.cert -inkey keys/ssh-example.private -out keys/ssh-example.p12 -name ssh-example -CAfile keys/repocache.qgears.com.cert -caname root -passout pass:verysec


