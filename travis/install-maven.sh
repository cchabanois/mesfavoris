#!/bin/bash
# maven version is 3.2.5 by default on travis. Polyglot projects need Maven 3.3.1

set -ev

# don't use archive.apache.org because it sometimes fails with 503. See https://issues.apache.org/jira/browse/INFRA-12996
# - wget --tries=20 --retry-connrefused --waitretry=1 https://archive.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip
wget http://apache.mirrors.ovh.net/ftp.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip
wget --no-check-certificate https://www.apache.org/dist/maven/maven-3/3.3.9/binaries/apache-maven-3.3.9-bin.zip.asc
wget --no-check-certificate https://www.apache.org/dist/maven/KEYS

gpg --import KEYS
gpg --verify apache-maven-3.3.9-bin.zip.asc apache-maven-3.3.9-bin.zip

unzip -qq apache-maven-3.3.9-bin.zip

export M2_HOME=$PWD/apache-maven-3.3.9
export PATH=$M2_HOME/bin:$PATH
