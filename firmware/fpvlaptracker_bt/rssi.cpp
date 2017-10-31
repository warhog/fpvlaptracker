#include <Arduino.h>
#include "rssi.h"

Rssi::Rssi(unsigned long interval) : interval(interval) {
    pinMode(A0, INPUT);
}

void Rssi::process() {
    static unsigned long timerRssi = 0L;
    if (timerRssi <= millis()) {
        unsigned int rssi = this->measure();
        this->currentRssiValue = this->filter(rssi);
        // restart rssi timer
        timerRssi = millis() + this->interval;
    }
}

unsigned int Rssi::getRssi() {
    return this->currentRssiValue;
}

unsigned int Rssi::filter(unsigned int rssi) {
    static unsigned int rssiOld = 0;
    unsigned int currentRssiStrength = rssiOld * 0.25 + rssi * 0.75;
    rssiOld = rssi;
    return currentRssiStrength;
}

/**
 * get current rssi strength
 * @return 
 */
unsigned int Rssi::measure() {
    // do multiple reads and calculate average value
    unsigned long sum = 0L;
    for (unsigned int i = 0; i < NUMBER_OF_RSSI_CYCLES; i++) {
        sum += analogRead(0);
    }
    sum /= NUMBER_OF_RSSI_CYCLES;
    int rssi = (unsigned int) (sum - this->getRssiOffset());
    if (rssi < 0) {
      rssi = 0;
    }
    return rssi;
}
