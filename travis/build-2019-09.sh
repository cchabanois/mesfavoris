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
	./mvnw clean deploy -P target-2019-09,release-composite -Dmaven.test.skip=true -Dgithub-update-repo=https://x-access-token:${GITHUB_PERSONAL_ACCESS_TOKEN}@github.com/cchabanois/mesfavoris-updates
elif [[ ${TRAVIS_BRANCH} = "master" ]]; then 
	echo "Build for standard commit on master"
	./mvnw clean verify -P target-2019-09,jacoco-report
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
