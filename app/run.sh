#!/bin/sh

# set the path to the platform tools
PATH=$PATH:/home/$USER/Android/Sdk/platform-tools

ionic cordova run android --device $1 $2 $3
if [ $? -ne 0 ]; then
    echo "failed to run"
    exit 1
fi
