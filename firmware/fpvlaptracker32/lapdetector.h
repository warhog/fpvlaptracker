#pragma once

#include <Arduino.h>
#include "storage.h"
#include "rssi.h"

namespace lap {

    class LapDetector {
    public:
        LapDetector(util::Storage *storage, Rssi *rssi);

        void init();
        boolean process();

        unsigned long getLastLapTime() const {
            return this->_lastLapTime;
        }

        void enableCalibrationMode() {
            this->_calibrationMode = true;
        }

        void disableCalibrationMode() {
            this->_calibrationMode = false;
        }

        boolean isCalibrating() const {
            return this->_calibrationMode;
        }

        unsigned int getLastLapRssi() const {
            return this->_lastLapRssi;
        }

    private:
        util::Storage* _storage;
        Rssi* _rssi;
        int _triggerValue;
        unsigned long _currentLapStart;
        unsigned long _lastLapTime;
        unsigned int _lastLapRssi;
        int _rssiPeak;
        unsigned long _rssiPeakTime;
        boolean _calibrationMode;
        int _calibrationOffset;
        int _triggerThreshold;
        int _triggerThresholdCalibration;
        boolean _reachedGate;
        unsigned long _lastLapEnd;
    };

}