#include "storage.h"

using namespace util;

//#define DEBUG

const char* CONFIG_VERSION = "020";
const unsigned int CONFIG_START = 32;

#ifndef max
    #define max(a,b) ({ __typeof__ (a) _a = (a); __typeof__ (b) _b = (b); _a > _b ? _a : _b; })
#endif

// do also adjust values in lapdetector constructor if you want to change defaults
// also think of changing the default values in the app
Storage::Storage() : _frequency(freq::LOWEST_FREQUENCY), _minLapTime(4000), _ssid("flt-base"), _wifiPassword("flt-base"),
    _triggerThresholdCalibration(240), _triggerThreshold(120), _calibrationOffset(30), _defaultVref(1100),
    _filterRatio(0.05), _filterRatioCalibration(0.005) {
}

void Storage::loadFactoryDefaults() {
    this->_frequency = 0;
    this->_minLapTime = 4000;
    this->_ssid = "flt-base";
    this->_wifiPassword = "flt-base";
    this->_triggerThreshold = 60;
    this->_triggerThresholdCalibration = 120;
    this->_calibrationOffset = 10;
    this->_filterRatio = 0.05;
    this->_filterRatioCalibration = 0.005;
    // skip default vref
}

void Storage::load() {
    if (EEPROM.read(CONFIG_START + 0) == CONFIG_VERSION[0] && EEPROM.read(CONFIG_START + 1) == CONFIG_VERSION[1] && EEPROM.read(CONFIG_START + 2) == CONFIG_VERSION[2]) {
#ifdef DEBUG
        Serial.println(F("load values from eeprom"));
#endif
        StorageEepromStruct storage;
        for (unsigned int t = 0; t < sizeof(storage); t++) {
            *((char*)&storage + t) = EEPROM.read(CONFIG_START + t);
        }
        this->_ssid = String(storage.ssid);
        this->_wifiPassword = String(storage.wifiPassword);
        this->_frequency = storage.frequency;
        this->_minLapTime = storage.minLapTime;
        this->_triggerThreshold = storage.triggerThreshold;
        this->_triggerThresholdCalibration = storage.triggerThresholdCalibration;
        this->_calibrationOffset = storage.calibrationOffset;
        this->_defaultVref = storage.defaultVref;
        this->_filterRatio = storage.filterRatio;
        this->_filterRatioCalibration = storage.filterRatioCalibration;
    } else {
#ifdef DEBUG
        Serial.println(F("load default values"));
#endif
        this->store();
    }
}

void Storage::store() {
#ifdef DEBUG
    Serial.println(F("store config to eeprom"));
#endif

    StorageEepromStruct storage;
    strncpy(storage.version, CONFIG_VERSION, 3);
    strncpy(storage.ssid, this->_ssid.c_str(), max(63, strlen(this->_ssid.c_str())));
    strncpy(storage.wifiPassword, this->_wifiPassword.c_str(), max(63, strlen(this->_wifiPassword.c_str())));
    storage.frequency = this->_frequency;
    storage.minLapTime = this->_minLapTime;
    storage.triggerThreshold = this->_triggerThreshold;
    storage.triggerThresholdCalibration = this->_triggerThresholdCalibration;
    storage.calibrationOffset = this->_calibrationOffset;
    storage.defaultVref = this->_defaultVref;
    storage.filterRatio = this->_filterRatio;
    storage.filterRatioCalibration = this->_filterRatioCalibration;

    for (unsigned int t = 0; t < sizeof(storage); t++) {
        EEPROM.write(CONFIG_START + t, *((char*)&storage + t));
    }
    EEPROM.commit();
}