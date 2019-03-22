#!/bin/bash

echo "fpvlaptracker raspbian install v1.0"
echo

if [[ $EUID -ne 0 ]]; then
   echo "this script must be run as root"
   exit 1
fi

if [[ $# -eq 0 ]]; then
    echo "usage: $0 <version>"
    echo "  version - version you want to install, e.g. 1.0.5"
    exit
fi

VERSION=$1

mkdir -p /home/pi/fpvlaptracker
cd /home/pi/fpvlaptracker

wget https://github.com/warhog/fpvlaptracker/releases/download/${VERSION}/fpvlaptracker-${VERSION}.zip
if [[ $? -ne 0 ]]; then
    echo "failed to download"
    exit 1
fi

unzip -f fpvlaptracker-${VERSION}.zip
if [[ $? -ne 0 ]]; then
    echo "failed to unzip"
    exit 1
fi

rm fpvlaptracker-${VERSION}.zip

chmod +x fpvlaptracker-${VERSION}.jar

sed -i "s/0.0.0/${VERSION}/g" fpvlaptracker.service
cp fpvlaptracker.service /lib/systemd/system/
chown 644 /lib/systemd/system/fpvlaptracker.service

systemctl daemon-reload
systemctl enable fpvlaptracker.service
if [[ $? -ne 0 ]]; then
    echo "failed to enable service"
    exit 1
fi

echo
echo "installation done, please reboot"
