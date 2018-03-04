#!/bin/bash

set -ev

if [[ ${TRAVIS_PULL_REQUEST} != "false" ]]; then
	echo "Build for Pull Request"
	./mvnw clean verify -P target-oxygen
elif [[ $TRAVIS_TAG =~ ^release.*$ ]]; then
	echo "Build for release : do nothing"
elif [[ ${TRAVIS_BRANCH} = "master" ]]; then 
	echo "Build for standard commit on master"
	./mvnw clean verify -P target-oxygen
else
	echo "Build for commit on a branch"
	./mvnw clean verify -P target-oxygen
fi
