#!/usr/bin/env bash

read -p "Enter access token (Required) : " accessToken
[ -z "$accessToken" ] && { echo "Error: Access Token cannot be empty!"; exit 1; }
stty -echo
echo

cd ../
java -jar $(find . -name "*org.wso2.ldif.scim*") "$accessToken"
