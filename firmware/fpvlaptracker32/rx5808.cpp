#include "rx5808.h"

using namespace radio;

//#define DEBUG

Rx5808::Rx5808(unsigned int pinSpiClock, unsigned int pinSpiData, unsigned int pinSpiSlaveSelect, unsigned int pinRssi) : 
    _pinSpiClock(pinSpiClock), _pinSpiData(pinSpiData), _pinSpiSlaveSelect(pinSpiSlaveSelect), _pinRssi(pinRssi),
	_scanLastRun(0L), _scanLastRssi(0), _scanState(scan_state::DONE), _scanChannelIndex(0) {

}

void Rx5808::init() {
#ifdef DEBUG
    Serial.println(F("setting up rx5808 ports"));
#endif
	pinMode(this->_pinSpiClock, OUTPUT);
	pinMode(this->_pinSpiData, OUTPUT);
	pinMode(this->_pinSpiSlaveSelect, OUTPUT);
}

void Rx5808::freq(unsigned int channelData) {
	this->_freq = channelData;
	// Second is the channel data from the lookup table
	// 20 bytes of register data are sent, but the MSB 4 bits are zeros
	// register address = 0x1, write, data0-15=channelData data15-19=0x0
	this->serialEnableHigh();
	this->serialEnableLow();

	// Register 0x1
	this->serialSendBit1();
	this->serialSendBit0();
	this->serialSendBit0();
	this->serialSendBit0();

	// Write to register
	this->serialSendBit1();

	// D0-D15
	//   note: loop runs backwards as more efficent on AVR
	for (uint8_t j = 16; j > 0; j--) {
		// Is bit high or low?
		if (channelData & 0x1) {
			this->serialSendBit1();
		} else {
			this->serialSendBit0();
		}

		// Shift bits along to check the next one
		channelData >>= 1;
	}

	// Remaining D16-D19
	for (uint8_t j = 4; j > 0; j--) {
		this->serialSendBit0();
	}

	// Finished clocking data in
	this->serialEnableHigh();
	delayMicroseconds(2);

	digitalWrite(this->_pinSpiSlaveSelect, LOW);
	digitalWrite(this->_pinSpiClock, LOW);
	digitalWrite(this->_pinSpiData, LOW);
}

void Rx5808::serialSendBit1() {
	digitalWrite(this->_pinSpiClock, LOW);
	delayMicroseconds(2);

	digitalWrite(this->_pinSpiData, HIGH);
	delayMicroseconds(2);
	digitalWrite(this->_pinSpiClock, HIGH);
	delayMicroseconds(2);

	digitalWrite(this->_pinSpiClock, LOW);
	delayMicroseconds(2);
}

void Rx5808::serialSendBit0() {
	digitalWrite(this->_pinSpiClock, LOW);
	delayMicroseconds(2);

	digitalWrite(this->_pinSpiData, LOW);
	delayMicroseconds(2);
	digitalWrite(this->_pinSpiClock, HIGH);
	delayMicroseconds(2);

	digitalWrite(this->_pinSpiClock, LOW);
	delayMicroseconds(2);
}

void Rx5808::serialEnableLow() {
	delayMicroseconds(2);
	digitalWrite(this->_pinSpiSlaveSelect, LOW);
	delayMicroseconds(2);
}

void Rx5808::serialEnableHigh() {
	delayMicroseconds(2);
	digitalWrite(this->_pinSpiSlaveSelect, HIGH);
	delayMicroseconds(2);
}

void Rx5808::scan() {
	if (this->_scanState == scan_state::SET) {
		this->_scanLastRun = millis();
		this->freq(freq::Frequency::getSPIFrequencyForChannelIndex(this->_scanChannelIndex));
		this->_scanState = scan_state::SCAN;
	} else if (this->_scanState == scan_state::SCAN) {
		if ((this->_scanLastRun + 75) < millis()) {
			unsigned long rssi = 0L;
			for (unsigned int i = 0; i < 20; i++) {
				rssi += analogRead(this->_pinRssi);
			}
			rssi /= 20;
			this->_scanLastRssi = static_cast<unsigned int>(rssi);
			this->_scanState = scan_state::DONE;
		}
	}
}