#!/bin/bash

set -ev

killall -SIGINT ngrok && echo "ngrok terminated" || true