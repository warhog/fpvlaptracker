#pragma once

#include <Arduino.h>
#include <EEPROM.h>

#include "frequency.h"

namespace util {

    class Storage {
    public:
        Storage();
        void load();
        void store();
        void loadFactoryDefaults();
        void setFrequency(unsigned int frequency) {
            this->_frequency = frequency;
        }
        unsigned int getFrequency() {
            return this->_frequency;
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

        void setDefaultVref(unsigned int defaultVref) {
            this->_defaultVref = defaultVref;
        }
        unsigned int getDefaultVref() {
            return this->_defaultVref;
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

        void setFilterRatio(double filterRatio) {
            this->_filterRatio = filterRatio;
        }
        double getFilterRatio() {
            return this->_filterRatio;
        }

        void setFilterRatioCalibration(double filterRatioCalibration) {
            this->_filterRatioCalibration = filterRatioCalibration;
        }
        double getFilterRatioCalibration() {
            return this->_filterRatioCalibration;
        }

    private:
        unsigned int _frequency;
        unsigned long _minLapTime;
        unsigned int _triggerThreshold;
        unsigned int _triggerThresholdCalibration;
        unsigned int _calibrationOffset;
        String _ssid;
        String _wifiPassword;
        unsigned int _defaultVref;
        double _filterRatio;
        double _filterRatioCalibration;

        struct StorageEepromStruct {
            char version[4];
            unsigned int frequency;
            unsigned long minLapTime;
            unsigned int triggerThreshold;
            unsigned int triggerThresholdCalibration;
            unsigned int calibrationOffset;
            char ssid[64];
            char wifiPassword[64];
            unsigned int defaultVref;
            double filterRatio;
            double filterRatioCalibration;
        };

    };

}