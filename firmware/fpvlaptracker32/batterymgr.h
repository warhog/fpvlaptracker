#pragma once

#include <Arduino.h>

namespace battery {

    const unsigned int NR_OF_MEASURES = 10;
    const double CORRECTION_FACTOR = 0.010473633;

    class BatteryMgr {
    public:
        BatteryMgr(uint8_t pin) : _pin(pin), _lastRun(0L), _nrOfMeasures(0), _sum(0L), _alarmVoltage(-1.0), _voltageReading(255.0), _shutdownVoltage(-1.0), _measuring(true), _cells(0) {
            pinMode(this->_pin, INPUT);
        }

        void detectCellsAndSetup();
        void measure();

        void setAlarmVoltage(double alarmVoltage) {
            this->_alarmVoltage = alarmVoltage;
        }

        void setShutdownVoltage(double shutdownVoltage) {
            this->_shutdownVoltage = shutdownVoltage;
        }

        bool isAlarm() {
            return this->_voltageReading <= this->_alarmVoltage;
        }

        bool isShutdown() {
            return this->_voltageReading <= this->_alarmVoltage;
        }

        double getVoltage() {
            return this->_voltageReading;
        }

        double getAlarmVoltage() {
            return this->_alarmVoltage;
        }

        double getShutdownVoltage() {
            return this->_shutdownVoltage;
        }

        unsigned int getCells() {
            return this->_cells;
        }

    private:
        uint8_t _pin;
        unsigned long _lastRun;
        unsigned int _nrOfMeasures;
        double _sum;
        double _voltageReading;
        double _alarmVoltage;
        double _shutdownVoltage;
        bool _measuring;
        unsigned int _cells;

    };

}