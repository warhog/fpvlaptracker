#include "ledcontrol.h"

using namespace ledio;
#define UINT_MAX 65535

LedControl::LedControl(const int pin) : _pin(0), _mode(modes::STATIC), _interval(1000L), _nextRun(0L),
    _blinkSequenceCount(0L), _blinkSequenceLength(0L), _blinkSequenceWait(0L), _blinkSequenceState(0) {
    this->_pin = pin;
    pinMode(this->_pin, OUTPUT);
}

void LedControl::toggle() {
    digitalWrite(this->_pin, !digitalRead(this->_pin));
}

void LedControl::off() {
    digitalWrite(this->_pin, HIGH);
}

void LedControl::on() {
    digitalWrite(this->_pin, LOW);
}

void LedControl::mode(modes mode) {
    if (this->_mode != mode) {
        if (mode == modes::OFF) {
            this->off();
        } else if (mode == modes::STATIC) {
            this->on();
        } else if (mode == modes::BLINK_SEQUENCE) {
            this->_blinkSequenceState = 0;
            this->off();
            this->scheduleNextRun(0L);
        }
    }
    this->_mode = mode;
}

void LedControl::blinkSequence(unsigned int count, unsigned long length, unsigned long wait) {
    this->_blinkSequenceCount = count;
    this->_blinkSequenceLength = length;
    this->_blinkSequenceWait = wait;
    this->_blinkSequenceState = 0;
    this->off();
    this->scheduleNextRun(0L);
}

bool LedControl::blinkSequenceDone() {
    return this->_blinkSequenceState == UINT_MAX;
}

void LedControl::run() {
    if (this->_mode == modes::BLINK) {
        if (this->isExpired()) {
            this->scheduleNextRun(this->_interval);
            this->toggle();
        }
    } else if (this->_mode == modes::BLINK_SEQUENCE) {
        if (this->isExpired()) {
            if (this->_blinkSequenceState == 0) {
                this->_blinkSequenceState++;
                this->on();
                this->scheduleNextRun(this->_blinkSequenceLength);
            } else if (this->_blinkSequenceState >= (this->_blinkSequenceCount * 2)) {
                this->_mode = modes::OFF;
                this->_blinkSequenceState = 0;
                this->_blinkSequenceState = UINT_MAX;
                this->off();
            } else {
                this->_blinkSequenceState++;
                if (this->_blinkSequenceState % 2 == 0) {
                    // state is even
                    this->on();
                    this->scheduleNextRun(this->_blinkSequenceLength);
                } else {
                    // state is odd
                    this->off();
                    this->scheduleNextRun(this->_blinkSequenceWait);
                }
            }
        }
    }
}