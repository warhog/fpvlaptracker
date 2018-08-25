#include "batterymgr.h"

using namespace battery;

//#define DEBUG

void BatteryMgr::measure() {
    if (((millis() - this->_lastRun) > 30000 && !this->_measuring) || ((millis() - this->_lastRun) > 10 && this->_measuring)) {
        unsigned int batteryRaw = analogRead(this->_pin);
#ifdef DEBUG
        Serial.printf("batteryRaw: %d\n", batteryRaw);
#endif
        this->_sum += batteryRaw;
        this->_nrOfMeasures++;
        if (this->_nrOfMeasures >= NR_OF_MEASURES) {
            this->_sum /= NR_OF_MEASURES;
            this->_voltageReading = this->_sum * CORRECTION_FACTOR;
#ifdef DEBUG
            Serial.printf("sum: %f, voltageReading: %f\n", this->_sum, this->_voltageReading);
#endif
            this->_nrOfMeasures = 0;
            this->_sum = 0.0;
            this->_lastRun = millis();
            this->_measuring = false;
        } else {
            this->_measuring = true;
        }
    }
}

void BatteryMgr::detectCellsAndSetup() {

    analogSetPinAttenuation(this->_pin, ADC_11db);
    delay(100);

    double sum = 0.0;
    for (unsigned int i = 0; i < NR_OF_MEASURES; i++) {
        unsigned int batteryRaw = analogRead(this->_pin);
        sum += batteryRaw;
        delay(10);
    }
    sum /= NR_OF_MEASURES;
#ifdef DEBUG
    Serial.printf("voltage raw: %f\n", sum);
#endif
    sum *= CORRECTION_FACTOR;
#ifdef DEBUG
    Serial.printf("voltage detect: %f\n", sum);
#endif
    // 2s 7,4-8,4 -> 7-9
    // 3s 11,1-12,6 -> 10-13
    // 4s 14,8-16,8 -> 14-17
    // 5s 18,5-21 -> 18-21,5
    // 6s 22,2-25,2 -> 22-26
    
    unsigned int cells = 0;
    if (sum > 7 && sum <= 10) {
        // 2s
        cells = 2;
        this->setAlarmVoltage(7.4);
        this->setShutdownVoltage(6.6);
    } else if (sum > 10 && sum < 13) {
        // 3s
        cells = 3;
        this->setAlarmVoltage(11.1);
        this->setShutdownVoltage(9.9);
    } else if (sum >= 13 && sum < 17) {
        // 4s
        cells = 4;
        this->setAlarmVoltage(14.8);
        this->setShutdownVoltage(13.2);
    } else if (sum >= 17 && sum < 21.5) {
        // 5s
        cells = 5;
        this->setAlarmVoltage(18.5);
        this->setShutdownVoltage(16.5);
    } else if (sum >= 21.5 && sum < 26) {
        // 6s
        cells = 6;
        this->setAlarmVoltage(22.2);
        this->setShutdownVoltage(19.8);
    } else {
        // failure
#ifdef DEBUG
        Serial.println(F("failed to detect voltage"));
#endif
    }

    this->_cells = cells;
#ifdef DEBUG
    Serial.printf("cells: %d\n", cells);
#endif

}