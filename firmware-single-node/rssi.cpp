#include <Arduino.h>
#include "rssi.h"

using namespace lap;

Rssi::Rssi(unsigned int pin) : _currentRssiRawValue(0), _currentRssiValue(0), _pin(pin), _currentRssiSmoothed(0.0f), _filterRatio(0.01f), analog(0, false) {
	pinMode(pin, INPUT);
}

void Rssi::process() {
	unsigned int rssi = analogRead(this->_pin);
	this->analog.update(rssi);

	this->_currentRssiRawValue = rssi;
	this->_currentRssiValue = (unsigned int)this->analog.getValue();
	// Serial.printf("%d, %f\n", this->_currentRssiValue, this->_currentRssiSmoothed);
}
