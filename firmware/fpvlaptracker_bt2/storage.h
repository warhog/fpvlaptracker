#pragma once
#include <Arduino.h>
#include <EEPROM.h>

namespace util {

    class Storage {
    public:
        Storage();
        void load();
        void store();
        void setChannelIndex(unsigned int channelIndex) {
            this->_channelIndex = channelIndex;
        }
        unsigned int getChannelIndex() {
            return this->_channelIndex;
        }

        void setMinLapTime(unsigned int minLapTime) {
            this->_minLapTime = minLapTime;
        }
        unsigned int getMinLapTime() {
            return this->_minLapTime;
        }

        void setRssiThreshold(unsigned int rssiThreshold) {
            this->_rssiThreshold = rssiThreshold;
        }
        unsigned int getRssiThreshold() {
            return this->_rssiThreshold;
        }

        void setRssiTriggerOffset(unsigned int rssiTriggerOffset) {
            this->_rssiTriggerOffset = rssiTriggerOffset;
        }
        unsigned int getRssiTriggerOffset() {
            return this->_rssiTriggerOffset;
        }

        void setSsid(String ssid) {
            if (ssid.length() > 64) {
                return;
            }
            this->_ssid = ssid;
        }
        String getSsid() {
            return this->_ssid;
        }

        void setWifiPassword(String password) {
            if (password.length() > 64) {
                return;
            }
            this->_wifiPassword = password;
        }
        String getWifiPassword() {
            return this->_wifiPassword;
        }

    private:
        unsigned int _channelIndex;
        unsigned long _minLapTime;
        unsigned int _rssiThreshold;
        unsigned int _rssiTriggerOffset;
        String _ssid;
        String _wifiPassword;

        struct StorageEepromStruct {
            char version[4];
            unsigned int channelIndex;
            unsigned long minLapTime;
            unsigned int rssiThreshold;
            unsigned int rssiTriggerOffset;
            char ssid[64];
            char wifiPassword[64];
        };

    };

}