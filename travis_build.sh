#!/bin/bash

set -ev

if [[ ${TRAVIS_PULL_REQUEST} != "false" ]]; then
	echo "Build for Pull Request"
	mvn clean verify
	STATUS=$?
	if [ $STATUS -eq 0 ]; then
		bash <(curl -s https://codecov.io/bash)
	fi
elif [[ $TRAVIS_TAG =~ ^release.*$ ]]; then
	echo "Build for release"
	# skip tests, you are not supposed to add code when creating a release, just add a tag
	mvn clean verify -P deploy,release -Dmaven.test.skip=true
else
	echo "Build for standard commit"
	mvn clean verify -P deploy
	STATUS=$?
	if [ $STATUS -eq 0 ]; then
		bash <(curl -s https://codecov.io/bash)
	fi
fi
