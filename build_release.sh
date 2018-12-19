#!/bin/bash

if [ $# -eq 0 ]; then
    echo "usage: $0 <version>"
    echo "  version - e.g. 1.0.5"
    exit
fi

VERSION=$1
CURRENT_BRANCH=`git branch | grep \* | cut -d ' ' -f2`
START_PATH=`pwd`

git checkout release/${VERSION} >/dev/null 2>&1
if [[ $? -gt 0 ]]; then
    echo "git branch release/${VERSION} not found"
    git checkout ${CURRENT_BRANCH}
    exit
fi
echo "build release version ${VERSION}"

gradle -Pversion=$1 build

mkdir -p build/release
cd build/release
rm -fr *

cp ../libs/fpvlaptracker-${VERSION}.jar ./
cp -r ../../release_files/* ./
zip -9 -r fpvlaptracker-${VERSION}.zip *
cp fpvlaptracker-${VERSION}.zip ${START_PATH}/fpvlaptracker-${VERSION}.zip
cd ${START_PATH}
