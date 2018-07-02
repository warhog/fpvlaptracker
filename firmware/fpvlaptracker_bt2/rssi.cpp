#include <Arduino.h>
#include "rssi.h"

using namespace lap;

Rssi::Rssi(unsigned int pin) : _currentRssiRawValue(0), _currentRssiValue(0), _rssiOffset(0), _pin(pin) {
	pinMode(pin, INPUT);
}

void Rssi::process() {
	if ((this->_lastRun + 1) < millis()) {
		this->_lastRun = millis();

		int rssi = analogRead(this->_pin);
		rssi -= this->getRssiOffset();
		if (rssi < 0) {
			rssi = 0;
		}
		this->_currentRssiRawValue = rssi;
		this->_currentRssiValue = static_cast<unsigned int>(static_cast<float>(this->_currentRssiValue) * 0.9 + static_cast<float>(this->_currentRssiRawValue) * 0.1);
	}
}
