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

        void setTriggerThresholdCalibration(unsigned int triggerThresholdCalibration) {
            this->_triggerThresholdCalibration = triggerThresholdCalibration;
        }
        unsigned int getTriggerThresholdCalibration() {
            return this->_triggerThresholdCalibration;
        }

        void setTriggerThreshold(unsigned int triggerThreshold) {
            this->_triggerThreshold = triggerThreshold;
        }
        unsigned int getTriggerThreshold() {
            return this->_triggerThreshold;
        }

        void setCalibrationOffset(unsigned int calibrationOffset) {
            this->_calibrationOffset = calibrationOffset;
        }
        unsigned int getCalibrationOffset() {
            return this->_calibrationOffset;
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
        unsigned int _triggerThreshold;
        unsigned int _triggerThresholdCalibration;
        unsigned int _calibrationOffset;
        String _ssid;
        String _wifiPassword;

        struct StorageEepromStruct {
            char version[4];
            unsigned int channelIndex;
            unsigned long minLapTime;
            unsigned int triggerThreshold;
            unsigned int triggerThresholdCalibration;
            unsigned int calibrationOffset;
            char ssid[64];
            char wifiPassword[64];
        };

    };

}