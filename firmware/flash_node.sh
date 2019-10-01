#!/bin/sh

if [[ $# -ne 2 ]]; then
    echo "$0 <port> <version>"
    exit 1
fi

echo "start flashing..."
esptool.py --chip esp32 --port $1 --baud 921600 --before default_reset --after hard_reset write_flash -z --flash_mode dio --flash_freq 80m --flash_size detect 0xe000 boot_app0.bin 0x1000 bootloader_qio_80m.bin 0x10000 firmware-${VERSION}.bin 0x8000 firmware-${VERSION}.partitions.bin
if [[ $? -ne 0 ]]; then
    echo "failed to flash"
    exit 1
fi

echo "done."
