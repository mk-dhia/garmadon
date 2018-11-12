#!/usr/bin/env bash

set -o pipefail
set -e
set -x

NEW_GARMADON_RELEASE=$1
cd "`dirname "$0"`/.."

# Create/Switch to release branch locally
git checkout release_$NEW_GARMADON_RELEASE || git checkout -b release_$NEW_GARMADON_RELEASE

# Bump release
mvn versions:set -DnewVersion=${NEW_GARMADON_RELEASE}
find . -name pom.xml.versionsBackup -delete
sed -i "s#<garmadon.version>.*</garmadon.version>#<garmadon.version>${NEW_GARMADON_RELEASE}</garmadon.version>#g" pom.xml

# Commit release
git add *pom.xml
git commit -a -m "Prepare release ${NEW_GARMADON_RELEASE}"