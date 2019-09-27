#!/bin/bash

if [ $# -eq 0 ]; then
    echo "usage: $0 <version>"
    echo "  version - e.g. 1.0.5"
    exit
fi

VERSION=$1
START_PATH=`pwd`

git checkout ${VERSION} >/dev/null
if [[ $? -gt 0 ]]; then
    echo "cannot checkout tag ${VERSION}"
    exit
fi
echo "build release version ${VERSION}"

gradle clean
gradle -Pversion=$1 build

mkdir -p build/release
cd build/release
rm -fr *

cp ../libs/fpvlaptracker-${VERSION}.jar ./
cp -r ../../lang/ ./
cp -r ../../release_files/* ./
sed -i "s/0.0.0/${VERSION}/g" ./fpvlaptracker.service
zip -9 -r fpvlaptracker-${VERSION}.zip *
cp fpvlaptracker-${VERSION}.zip ${START_PATH}/fpvlaptracker-${VERSION}.zip
cd ${START_PATH}

git checkout master
