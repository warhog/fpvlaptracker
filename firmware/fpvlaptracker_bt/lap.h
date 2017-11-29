#ifndef LAP
#define LAP

#include <Arduino.h>

class Lap {
private:
    unsigned long currentLapStart = 0;
    boolean isLocked = false;
    unsigned long minLapTime = 8000L;
    unsigned int rssiThresholdLow = 100;
    unsigned int rssiThresholdHigh = 140;
    unsigned long lastLapTime = 0L;
    unsigned int rssiPeak = 0;
    unsigned long rssiPeakTime = 0L;
    
public:
    boolean process(unsigned int currentRssiStrength);

    unsigned long getMinLapTime() const {
        return minLapTime;
    }

    void setMinLapTime(unsigned long minLapTime) {
        this->minLapTime = minLapTime;
    }

    unsigned int getRssiThresholdHigh() const {
        return rssiThresholdHigh;
    }

    void setRssiThresholdHigh(unsigned int rssiThresholdHigh) {
        this->rssiThresholdHigh = rssiThresholdHigh;
    }

    unsigned int getRssiThresholdLow() const {
        return rssiThresholdLow;
    }

    void setRssiThresholdLow(unsigned int rssiThresholdLow) {
        this->rssiThresholdLow = rssiThresholdLow;
    }

    bool isLock() const {
        return isLocked;
    }
    
    unsigned long getLastLapTime() const {
        return lastLapTime;
    }

};

#endif
