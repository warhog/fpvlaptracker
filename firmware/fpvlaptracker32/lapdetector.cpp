#include "lapdetector.h"

using namespace lap;

//#define DEBUG
//#define MEASURE

LapDetector::LapDetector(util::Storage *storage, Rssi *rssi) : _storage(storage), _triggerValue(10), _currentLapStart(0),
    _lastLapTime(0), _rssiPeak(0), _rssiPeakTime(0), _calibrationMode(false), _calibrationOffset(15),
	_reachedGate(false), _rssi(rssi), _triggerThreshold(60), _triggerThresholdCalibration(120), _lastLapRssi(0),
	_lastLapEnd(0)
	{

}

void LapDetector::init() {
	this->_calibrationOffset = this->_storage->getCalibrationOffset();
	this->_triggerThreshold = this->_storage->getTriggerThreshold();
	this->_triggerThresholdCalibration = this->_storage->getTriggerThresholdCalibration();
}

/**
 * process lap detection
 * @return true if new lap is detected, false otherwise
 */
boolean LapDetector::process() {
    // if last lap end with min lap time is lower than millis allow next lap
    if (millis() >= (this->_lastLapEnd + this->_storage->getMinLapTime())) {
#ifdef MEASURE
		Serial.print(F("VAR: lap_calibrationMode="));
		Serial.println(this->_calibrationMode);
		Serial.print(F("VAR: lap_reachedGate="));
		Serial.println(this->_reachedGate);
		Serial.print(F("VAR: lap_triggerValue="));
		Serial.println(this->_triggerValue);
#endif
		if (!this->_reachedGate && this->_rssi->getRssi() > this->_triggerValue) {
			// not reached the gate and higher than trigger value -> reached the gate :)
			this->_reachedGate = true;
#ifdef DEBUG
			Serial.println(F("reached the gate"));
#endif
#ifdef MEASURE
			Serial.println(F("LAP: reached the gate"));
#endif
		}

		// rssi peak to peak detection
#ifdef MEASURE
		Serial.print(F("VAR: lap_rssiPeak="));
		Serial.println(this->_rssiPeak);
#endif
		if (this->_rssi->getRssi() > this->_rssiPeak) {
			this->_rssiPeak = this->_rssi->getRssi();
			this->_rssiPeakTime = millis();
#ifdef MEASURE
			Serial.print(F("LAP: new _rssiPeak="));
			Serial.println(this->_rssiPeak);
#endif
#ifdef DEBUG
			Serial.print(F("new max rssi: "));
			Serial.println(this->_rssiPeak);
#endif
		}

		if (this->_reachedGate) {

#ifdef MEASURE
			Serial.print(F("VAR: lap_calibrationOffset="));
			Serial.println(this->_calibrationOffset);
#endif
			if (this->_calibrationMode && static_cast<int>(this->_rssi->getRssi()) >= this->_calibrationOffset) {
				if ((static_cast<int>(this->_rssi->getRssi()) - this->_calibrationOffset) > this->_triggerValue) {
#ifdef MEASURE
					Serial.print(F("LAP: old _triggerValue="));
					Serial.println(this->_triggerValue);
					Serial.print(F("LAP: rssi_in_new_triggerValue="));
					Serial.println(this->_rssi->getRssi());
					Serial.print(F("LAP: rssi - calibrationOffset="));
					Serial.println(static_cast<int>(this->_rssi->getRssi()) - this->_calibrationOffset);
#endif
#ifdef DEBUG
					Serial.print(F("set new trigger value, triggerValue: "));
					Serial.print(this->_triggerValue);
					Serial.print(F(", rssi - calibrationOffset: "));
					Serial.println(static_cast<int>(this->_rssi->getRssi()) - this->_calibrationOffset);
#endif
					this->_triggerValue = static_cast<int>(this->_rssi->getRssi()) - this->_calibrationOffset;
#ifdef MEASURE
					Serial.print(F("VAR: lap_triggerValue="));
					Serial.println(this->_triggerValue);
					Serial.print(F("LAP: new _triggerValue="));
					Serial.println(this->_triggerValue);
#endif
				}
			}

			int lowerThreshold = (this->_calibrationMode) ? this->_triggerThresholdCalibration : this->_triggerThreshold;
#ifdef MEASURE
			Serial.print(F("VAR: lap_lowerThreshold="));
			Serial.println(lowerThreshold);
			Serial.print(F("VAR: lap_endlapcondition="));
			Serial.println(this->_triggerValue - lowerThreshold);
#endif
			if (this->_triggerValue > lowerThreshold && this->_rssi->getRssi() < (this->_triggerValue - lowerThreshold)) {
#ifdef MEASURE
				Serial.println(F("LAP: leaving gate"));
				Serial.print(F("LAP: rssi="));
				Serial.println(this->_rssi->getRssi());
				Serial.print(F("LAP: _triggerValue="));
				Serial.println(this->_triggerValue);
				Serial.print(F("LAP: lowerThreshold="));
				Serial.println(lowerThreshold);
#endif
#ifdef DEBUG
				Serial.print(F("leaving gate, "));
				Serial.print(F("rssi: "));
				Serial.print(this->_rssi->getRssi());
				Serial.print(F(", triggerValue: "));
				Serial.print(this->_triggerValue);
				Serial.print(F(", lowerThreshold: "));
				Serial.println(lowerThreshold);
#endif
				this->_reachedGate = false;
    			this->_lastLapTime = this->_rssiPeakTime - this->_currentLapStart;
				this->_lastLapRssi = this->_rssi->getRssi();
				this->_currentLapStart = this->_rssiPeakTime;
				this->_lastLapEnd = millis();
				this->_rssiPeak = 0;
				this->_rssiPeakTime = 0L;
				this->disableCalibrationMode();
    			return true;

			}

		}
  	}
  	return false;
}
