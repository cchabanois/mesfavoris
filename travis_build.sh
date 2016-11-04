#!/bin/bash

set -ev

if [[ ${TRAVIS_PULL_REQUEST} != "false" ]]; then
	echo "Build for Pull Request"
	mvn clean verify
elif [[ $TRAVIS_TAG =~ ^release.*$ ]]; then
	echo "Build for release"
	mvn clean verify -P deploy,release
else
	echo "Build for standard commit"
	mvn clean verify -P deploy
fi
