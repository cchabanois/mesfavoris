#!/bin/bash

set -ev

wget -O ngrok.zip https://bin.equinox.io/c/4VmDzA7iaHb/ngrok-stable-linux-amd64.zip
unzip ngrok.zip
./ngrok tcp 5005 --authtoken=$NGROK_AUTH_TOKEN --log=stdout --log-level=debug | grep "tcp.ngrok.io" &
