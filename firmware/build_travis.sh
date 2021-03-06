#!/bin/bash
# heavily inspired by https://github.com/adafruit/travis-ci-arduino/blob/master/install.sh
ARDUINO_IDE_VERSION="1.8.10"

VERSION=""
if [[ -z "${TRAVIS_TAG}" ]]; then
    # no travis tag, use commit
    VERSION=${TRAVIS_COMMIT}
else
    VERSION=${TRAVIS_TAG}
fi
echo "building version ${VERSION}"

sed -i "s/NO_TRAVIS_BUILD/${VERSION}/g" version.h

# use the latest available tag here to make sure node can register on backend
VERSION_SAFE=$(git describe --tags | awk '{split($0,a,"-"); print a[1]}')
sed -i "s/FLT32-R0.0.0/FLT32-R${VERSION_SAFE}/g" version.h

# make display available for arduino CLI
/sbin/start-stop-daemon --start --quiet --pidfile /tmp/custom_xvfb_1.pid --make-pidfile --background --exec /usr/bin/Xvfb -- :1 -ac -screen 0 1280x1024x16
sleep 3
export DISPLAY=:1.0

echo "install arduino ide"
wget --quiet https://downloads.arduino.cc/arduino-${ARDUINO_IDE_VERSION}-linux64.tar.xz
if [[ $? -ne 0 ]]; then
    echo "failed to download ide"
    exit 1
fi
echo "unpack arduino ide"
[ ! -d ./arduino_ide/ ] && mkdir ./arduino_ide
tar xf arduino-${ARDUINO_IDE_VERSION}-linux64.tar.xz -C ./arduino_ide/ --strip-components=1
if [[ $? -ne 0 ]]; then
    echo "failed to unpack ide"
    exit 1
fi

PATH="./arduino_ide:${PATH}"

echo "setting esp32 board support url"
DEPENDENCY_OUTPUT=$(arduino --pref "boardsmanager.additional.urls=https://dl.espressif.com/dl/package_esp32_index.json" --save-prefs 2>&1)
if [ $? -ne 0 ]; then
    echo "failed to set esp32 board support url: ${DEPENDENCY_OUTPUT}"
    exit 1
fi

echo "removing esp32 cache"
rm -rf ~/.arduino15/packages/esp32

echo "install esp32 board support"
DEPENDENCY_OUTPUT=$(arduino --install-boards esp32:esp32 2>&1)
if [ $? -ne 0 ]; then
    echo "failed to install esp32 board support: ${DEPENDENCY_OUTPUT}"
    exit 1
fi

echo "install library ArduinoJson"
DEPENDENCY_OUTPUT=$(arduino --install-library ArduinoJson:6.11.5 > /dev/null 2>&1)
if [ $? -ne 0 ]; then
    echo "failed to install ArduinoJson: ${DEPENDENCY_OUTPUT}"
    exit 1
fi

echo "install library ResponsiveAnalogRead"
DEPENDENCY_OUTPUT=$(arduino --install-library ResponsiveAnalogRead:1.2.1 > /dev/null 2>&1)
if [ $? -ne 0 ]; then
    echo "failed to install ResponsiveAnalogRead: ${DEPENDENCY_OUTPUT}"
    exit 1
fi

echo "setting board"
PLATFORM_OUTPUT=$(arduino --board esp32:esp32:esp32:PSRAM=disabled,PartitionScheme=min_spiffs,CPUFreq=240,FlashMode=qio,FlashFreq=80,FlashSize=4M,UploadSpeed=921600,DebugLevel=none --save-prefs 2>&1)
if [ $? -ne 0 ]; then
    echo "failed to set board: ${PLATFORM_OUTPUT}"
    exit 1
fi

echo "building"
BUILD_PATH=../build-firmware
BUILD_OUTPUT=$(arduino --verify --pref build.path=${BUILD_PATH} --preserve-temp-files firmware.ino 2>&1)
if [ $? -ne 0 ]; then
    echo "failed to build: ${BUILD_OUTPUT}"
    exit 1
fi

echo "renaming binary"
cp ${BUILD_PATH}/firmware.ino.bin ${BUILD_PATH}/firmware-${VERSION}.bin
cp ${BUILD_PATH}/firmware.ino.partitions.bin ${BUILD_PATH}/firmware-${VERSION}.partitions.bin

zip -9 -j ${BUILD_PATH}/fpvlaptracker-firmware-${VERSION}.zip ${BUILD_PATH}/firmware-${VERSION}.bin ${BUILD_PATH}/firmware-${VERSION}.partitions.bin ~/.arduino15/packages/esp32/hardware/esp32/1.0.3/tools/sdk/bin/bootloader_qio_80m.bin ~/.arduino15/packages/esp32/hardware/esp32/1.0.3/tools/partitions/boot_app0.bin ./flash_node.sh

echo "done"
