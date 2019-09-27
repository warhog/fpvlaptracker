#!/bin/bash

START_PATH=`pwd`

VERSION=""
if [[ -z "${TRAVIS_TAG}" ]]; then
    # no travis tag, use commit
    VERSION=${TRAVIS_COMMIT}
else
    VERSION=${TRAVIS_TAG}
fi
echo "building version ${VERSION}"

gradle clean
gradle -Pversion=${VERSION} build

mkdir -p build/release
cd build/release
rm -fr *

cp ../libs/fpvlaptracker-${VERSION}.jar ./
cp -r ../../lang/ ./
cp -r -v ../../release_files/* ./
tree .
sed -i "s/0.0.0/${VERSION}/g" ./fpvlaptracker.service
zip -9 -r fpvlaptracker-${VERSION}.zip *
cp fpvlaptracker-${VERSION}.zip ${START_PATH}/fpvlaptracker-${VERSION}.zip
cd ${START_PATH}

