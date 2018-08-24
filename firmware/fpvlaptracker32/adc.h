#pragma once

#define DEBUG

class Adc {
public:
    Adc(uint8_t pin) : _pin(pin), _lastRun(0L), _nrOfMeasures(0), _sum(0L), _correctionOffset(0.0), _correctionFactor(0.0), _alarmVoltage(11.0), _voltageReading(0.0) {
        pinMode(this->_pin, INPUT);
    }

    void measure() {
        if ((millis() - this->_lastRun) > 1000) {
            unsigned int batteryRaw = analogRead(this->_pin);
#ifdef DEBUG
            Serial.print("batteryRaw: ");
            Serial.println(batteryRaw);
#endif
            this->_sum += batteryRaw;
            this->_nrOfMeasures++;
            if (this->_nrOfMeasures >= 9) {
                this->_sum /= 10;
                this->_voltageReading = this->_sum * 0.0087890625; // 36V / 4096
                this->_voltageReading *= this->_correctionFactor;
                this->_voltageReading += this->_correctionOffset;
#ifdef DEBUG
                Serial.print("sum: ");
                Serial.println(this->_sum);
                Serial.print("voltageReading: ");
                Serial.println(this->_voltageReading);
#endif
                this->_nrOfMeasures = 0;
                this->_lastRun = millis();
            }
        }
    }

    void setCorrectionOffset(double correctionOffset) {
        this->_correctionOffset = correctionOffset;
    }
    void setCorrectionFactor(double correctionFactor) {
        this->_correctionFactor = correctionFactor;
    }

    void setAlarmVoltage(double alarmVoltage) {
        this->_alarmVoltage = alarmVoltage;
    }

    bool alarmHandler() {
        return this->_voltageReading <= this->_alarmVoltage;
    }

private:
    uint8_t _pin;
    unsigned long _lastRun;
    unsigned int _nrOfMeasures;
    unsigned long _sum;
    double _voltageReading;
    double _correctionOffset;
    double _correctionFactor;
    double _alarmVoltage;

};