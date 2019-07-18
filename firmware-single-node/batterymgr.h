#pragma once

#include <Arduino.h>
#include <driver/adc.h>
#include <esp_adc_cal.h>

#include "storage.h"

//#define DEBUG

namespace battery {

    const unsigned int NR_OF_MEASURES = 100;
    const double CONVERSATION_FACTOR = 5.778132482;
    //const unsigned int DEFAULT_VREF = 1089;

    class BatteryMgr {
    public:
        BatteryMgr(uint8_t pin, util::Storage *storage) : _pin(pin), _lastRun(0L), _nrOfMeasures(0), _sum(0L), _alarmVoltage(-1.0), _voltageReading(255.0), _shutdownVoltage(-1.0), _measuring(true), _cells(0), _chan(ADC1_CHANNEL_4), _storage(storage), _defaultVref(1100) {
            pinMode(this->_pin, INPUT);

            this->_adcChars = new esp_adc_cal_characteristics_t;
            switch (this->_pin) {
            case 32:
                this->_chan = ADC1_CHANNEL_4;
                break;
            case 33:
                this->_chan = ADC1_CHANNEL_5;
                break;
            case 34:
                this->_chan = ADC1_CHANNEL_6;
                break;
            case 35:
                this->_chan = ADC1_CHANNEL_7;
                break;
            case 36:
                this->_chan = ADC1_CHANNEL_3;
                break;
            case 39:
                this->_chan = ADC1_CHANNEL_0;
                break;
            default:
                for (;;) {}
            }

        }

        void detectCellsAndSetup();
        void measure();

        void enableVrefOutput() {
#ifdef DEBUG
            Serial.println(F("enabling vref output"));
#endif
            esp_err_t status = adc2_vref_to_gpio(GPIO_NUM_27);
#ifdef DEBUG
            if (status == ESP_OK) {
                Serial.println(F("v_ref routed to GPIO"));
            } else {
                Serial.println(F("failed to route v_ref"));
            }
#endif
        }

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
            return this->_voltageReading <= this->_shutdownVoltage;
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
        adc1_channel_t _chan;
        esp_adc_cal_characteristics_t *_adcChars;
        util::Storage *_storage;
        unsigned int _defaultVref;
    };

}