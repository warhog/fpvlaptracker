#pragma once

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

    unsigned int getRssiOffset() const {
      return this->_rssiOffset;
    }

    void setRssiOffset(unsigned int rssiOffset) {
      this->_rssiOffset = rssiOffset;
      this->process();
      this->_currentRssiValue = _currentRssiRawValue;
    }


  private:
    unsigned int _currentRssiValue;
    unsigned int _currentRssiRawValue;
    unsigned int _rssiOffset;
    unsigned long _lastRun;
    unsigned int _pin;
  };

}