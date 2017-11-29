#ifndef RSSI
#define RSSI

class Rssi {
private:
  unsigned int currentRssiValue = 0;
  unsigned int currentRssiRawValue = 0;
  unsigned int rssiOffset = 50;

  void filter();
  unsigned int measure();

public:
  Rssi();
  void process();
  unsigned int scan();

  unsigned int getRssi() {
    return this->currentRssiValue;
  }

  unsigned int getRssiRaw() {
    return this->currentRssiRawValue;
  }

  unsigned int getRssiOffset() const {
    return rssiOffset;
  }

  void setRssiOffset(unsigned int rssiOffset) {
    this->rssiOffset = rssiOffset;
  }

};
#endif
