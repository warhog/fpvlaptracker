#pragma once

#include <Arduino.h>
#include "subscriber.h"

//#define DEBUG

namespace statemanagement {

    enum class state_enum {
        STARTUP,
        CALIBRATION,
        CALIBRATION_DONE,
        SCAN,
        RACE,
        RSSI,
        RESTORE_STATE,
        ERROR
    };

    class StateManager : public pubsub::Subscriber<state_enum> {
    public:
        StateManager() : _state(state_enum::STARTUP), _storeState(state_enum::STARTUP) {
        }

        String toString(state_enum state) {
            if (state == state_enum::CALIBRATION) {
                return "Calibration";
            } else if (state == state_enum::RACE) {
                return "Race";
            } else if (state == state_enum::ERROR) {
                return "Error";
            } else if (state == state_enum::SCAN) {
                return "Scan";
            } else if (state == state_enum::RSSI) {
                return "RSSI";
            } else if (state == state_enum::STARTUP) {
                return "Startup";
            } else {
                return "Unknown";
            }
        }

        boolean isStateStartup() {
            return this->_state == state_enum::STARTUP;
        }

        boolean isStateCalibration() {
            return this->_state == state_enum::CALIBRATION;
        }

        boolean isStateCalibrationDone() {
            return this->_state == state_enum::CALIBRATION_DONE;
        }

        boolean isStateScan() {
            return this->_state == state_enum::SCAN;
        }

        boolean isStateError() {
            return this->_state == state_enum::ERROR;
        }

        boolean isStateRace() {
            return this->_state == state_enum::RACE;
        }

        boolean isStateRssi() {
            return this->_state == state_enum::RSSI;
        }

        state_enum getState() {
            return this->_state;
        }

        void setState(state_enum state) {
            if (state != this->_state) {
#ifdef DEBUG
                Serial.print(F("change state to "));
                Serial.println(this->toString(state));
#endif
                this->storeState();
                this->_state = state;
            }
        }

        void storeState() {
            this->_storeState = this->_state;
#ifdef DEBUG
            Serial.print(F("stored state "));
            Serial.println(this->toString(this->_storeState));
#endif
        }
        
        void restoreState() {
            this->_state = this->_storeState;
        }

        void update(state_enum state){
            if (state == state_enum::RESTORE_STATE) {
#ifdef DEBUG
                Serial.print(F("restore state request to "));
                Serial.println(this->toString(this->_storeState));
#endif
                this->setState(this->_storeState);
                return;
            }
#ifdef DEBUG
            Serial.print(F("state change request to "));
            Serial.println(this->toString(state));
#endif
            this->setState(state);
        }
        
    private:
        state_enum _state;
        state_enum _storeState;

    };

}