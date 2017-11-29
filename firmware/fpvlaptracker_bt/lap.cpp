#include "lap.h"

//#define DEBUG

/**
 * process lap detection
 * @return true if new lap is detected, false otherwise
 */
boolean Lap::process(unsigned int currentRssiStrength) {
  unsigned long temporaryLapTime = millis() - this->currentLapStart;
  if (temporaryLapTime >= this->minLapTime) {
    if (!this->isLocked) {
      // currently not locked
      if (currentRssiStrength >= this->rssiThresholdHigh) {
        // rssi higher than upper threshold value -> start detection of new lap
        this->isLocked = true;
#ifdef DEBUG
        Serial.print("locked, rssi: ");
        Serial.println(currentRssiStrength);
#endif
      }
    } else {
      // currently locked
      if (currentRssiStrength >= this->rssiThresholdLow) {
        // is locked and rssi higher than upper threshold value -> go on with lap detection
        if (currentRssiStrength > this->rssiPeak) {
          this->rssiPeak = currentRssiStrength;
          this->rssiPeakTime = millis();
#ifdef DEBUG
          Serial.print("new max rssi: ");
          Serial.println(this->rssiPeak);
#endif
        } else {
#ifdef DEBUG
          Serial.print("rssi: ");
          Serial.println(currentRssiStrength);
#endif
        }
      } else if (currentRssiStrength <= this->rssiThresholdLow) {
        this->lastLapTime = this->rssiPeakTime - this->currentLapStart;
        this->currentLapStart = this->rssiPeakTime;
        this->isLocked = false;
        this->rssiPeak = 0;
        this->rssiPeakTime = 0L;
#ifdef DEBUG
        Serial.print("unlocked, peak rssi: ");
        Serial.print(this->rssiPeak);
        Serial.print(", rssi now: ");
        Serial.println(currentRssiStrength);
#endif
        return true;
      }
    }
  }
  return false;
}
