#!/bin/bash

set -ev

sh -e /etc/init.d/xvfb start
metacity --sm-disable --replace 2> metacity.err &
