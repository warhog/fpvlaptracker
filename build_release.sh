#!/bin/bash

if [ $# -eq 0 ]; then
    echo "usage: $0 <version>"
    echo "  version - e.g. 1.0.5"
    exit
fi

VERSION=$1

git checkout release/${VERSION} >/dev/null 2>&1
if [[ $? -gt 0 ]]; then
    echo "git branch release/${VERSION} not found"
    exit
fi
echo "build release version ${VERSION}"

gradle -Pversion=$1 build
