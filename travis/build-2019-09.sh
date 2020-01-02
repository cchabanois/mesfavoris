#!/bin/bash

set -ev

if [[ ${TRAVIS_PULL_REQUEST} != "false" ]]; then
	echo "Build for Pull Request"
	./mvnw clean verify -P target-2019-09,jacoco-report
	STATUS=$?
	if [ $STATUS -eq 0 ]; then
		bash <(curl -s https://codecov.io/bash)
	fi
elif [[ $TRAVIS_TAG =~ ^release.*$ ]]; then
	echo "Build for release"
	# skip tests, you are not supposed to add code when creating a release, just add a tag
	./mvnw clean verify -P deploy,release -Dmaven.test.skip=true
elif [[ ${TRAVIS_BRANCH} = "master" ]]; then 
	echo "Build for standard commit on master"
	./mvnw clean verify -P target-2019-09,jacoco-report,deploy
	STATUS=$?
	if [ $STATUS -eq 0 ]; then
		bash <(curl -s https://codecov.io/bash)
	fi
else
	echo "Build for commit on a branch"
	./mvnw clean verify -P target-2019-09,jacoco-report
	STATUS=$?
	if [ $STATUS -eq 0 ]; then
		bash <(curl -s https://codecov.io/bash)
	fi
fi
