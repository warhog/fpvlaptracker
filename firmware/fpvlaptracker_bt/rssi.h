#ifndef RSSI
#define RSSI

// defines the number of rssi measure cycles (average is calculated over that number of cycles)
const unsigned int NUMBER_OF_RSSI_CYCLES = 3;

class Rssi {
private:
    unsigned int currentRssiValue = 0;
    unsigned int rssiOffset = 50;
    unsigned long interval = 1000L;

    unsigned int filter(unsigned int rssi);
    unsigned int measure();

public:
    Rssi(unsigned long interval);
    void process();
    unsigned int getRssi();

    unsigned int getRssiOffset() const {
        return rssiOffset;
    }

    void setRssiOffset(unsigned int rssiOffset) {
        this->rssiOffset = rssiOffset;
    }

};
#endif