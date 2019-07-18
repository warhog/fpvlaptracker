#pragma once
#include <Arduino.h>

namespace ledio {

    enum class modes { OFF, STATIC, BLINK, BLINK_SEQUENCE };

    class LedControl {
    public:
        LedControl(const int pin);
        void toggle();
        void on();
        void off();
        void mode(modes mode);
        void interval(unsigned long interval) {
            this->_interval = interval;
        }
        void oneshot(unsigned long length = 100) {
            this->blinkSequence(1, length, 1);
            this->_mode = modes::BLINK_SEQUENCE;
        }
        void blinkSequence(unsigned int count, unsigned long length, unsigned long wait);
        bool blinkSequenceDone();
        void run();
        void scheduleNextRun(unsigned long nextRun) {
            this->_nextRun = millis() + nextRun;
        }
        bool isExpired() {
            return this->_nextRun <= millis();
        }

    private:
        int _pin;
        modes _mode;
        unsigned long _interval;
        unsigned long _nextRun;
        unsigned long _blinkSequenceCount;
        unsigned long _blinkSequenceLength;
        unsigned long _blinkSequenceWait;
        unsigned int _blinkSequenceState;
    };

}