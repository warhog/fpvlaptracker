#!/bin/sh

rm -f flt.apk

# set the path to the platform tools
PATH=$PATH:/home/$USER/Android/Sdk/platform-tools

ionic cordova build --release --prod android
if [ $? -ne 0 ]; then
    echo "failed to build"
    exit 1
fi


# sign the jar file
jarsigner -verbose -sigalg SHA1withRSA -digestalg SHA1 -keystore flt-release-key.keystore platforms/android/build/outputs/apk/android-release-unsigned.apk alias_name
if [ $? -ne 0 ]; then
    echo "failed to sign jar"
    exit 1
fi

# run zipalignment
/home/$USER/Android/Sdk/build-tools/28.0.0/zipalign -v 4 platforms/android/build/outputs/apk/android-release-unsigned.apk flt.apk
if [ $? -ne 0 ]; then
    echo "failed to align zip file"
    exit 1
fi

