#!/bin/bash

set -ev

if [[ ${TRAVIS_PULL_REQUEST} != "false" ]]; then
	echo "Build for Pull Request"
	./mvnw clean verify -P target-oxygen
elif [[ $TRAVIS_TAG =~ ^release.*$ ]]; then
	echo "Build for release : do nothing"
else
	echo "Build for standard commit"
	./mvnw clean verify -P target-oxygen
fi
