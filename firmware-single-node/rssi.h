#pragma once

#include <ResponsiveAnalogRead.h>

namespace lap {

	class Rssi {
	public:
		Rssi(unsigned int pin);
		void process();

		unsigned int getRssi() {
			return this->_currentRssiValue;
		}

		unsigned int getRssiRaw() {
			return this->_currentRssiRawValue;
		}

		void setFilterRatio(double ratio) {
			this->_filterRatio = ratio;
		}

	private:
		double _currentRssiSmoothed;
		double _filterRatio;
		unsigned int _currentRssiValue;
		unsigned int _currentRssiRawValue;
		unsigned long _lastRun;
		unsigned int _pin;
		ResponsiveAnalogRead analog;

	};

}