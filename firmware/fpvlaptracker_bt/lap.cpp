#include "lap.h"

/**
 * process lap detection
 * @return true if new lap is detected, false otherwise
 */
boolean Lap::process(unsigned int currentRssiStrength) {
    unsigned long temporaryLapTime = millis() - currentLapStart;
    if (temporaryLapTime >= this->minLapTime) {
        if (this->isLocked && currentRssiStrength <= this->rssiThresholdLow) {
            this->isLocked = false;
        } else if (!isLocked && currentRssiStrength >= this->rssiThresholdHigh) {
            this->isLocked = true;
            unsigned long currentLapTime = millis() - currentLapStart;
            this->currentLapStart = millis();
            return true;
        }
    }
    
    return false;
}