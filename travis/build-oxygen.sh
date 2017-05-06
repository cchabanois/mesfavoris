#!/bin/bash

set -ev

if [[ ${TRAVIS_PULL_REQUEST} != "false" ]]; then
	echo "Build for Pull Request"
	mvn clean verify -P target-oxygen
elif [[ $TRAVIS_TAG =~ ^release.*$ ]]; then
	echo "Build for release : do nothing"
else
	echo "Build for standard commit"
	mvn clean verify -P target-oxygen
fi
