#include <Arduino.h>
#include "rssi.h"

Rssi::Rssi() {
    pinMode(A0, INPUT);
}

void Rssi::process() {
  this->currentRssiRawValue = this->measure();
  this->filter();
}

unsigned int Rssi::scan() {
  return this->measure();
}

void Rssi::filter() {
  this->currentRssiValue = this->currentRssiValue * 0.75 + this->currentRssiRawValue * 0.25;
}

/**
 * get current rssi strength
 * @return 
 */
unsigned int Rssi::measure() {
  int rssi = analogRead(0) - this->getRssiOffset();
  if (rssi < 0) {
    rssi = 0;
  }
  return rssi;
}
