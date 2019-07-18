#pragma once

#include <Arduino.h>
#include "storage.h"
#include "rssi.h"
#include "frequency.h"
#include "rx5808.h"
#include "statemanager.h"
#include "lapdetector.h"
#include "batterymgr.h"

namespace comm {

    class Comm {
    public:
        Comm(util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808, lap::LapDetector *lapDetector, battery::BatteryMgr *batteryMgr, const char *version, statemanagement::StateManager *stateManager, unsigned long *loopTime);
        void reg();
        void lap();
        int connect();
        bool isConnected() const {
            return this->_connected;
        }
        void disconnect();
    protected:
        util::Storage *_storage;
        lap::Rssi *_rssi;
        radio::Rx5808 *_rx5808;
        lap::LapDetector *_lapDetector;
        battery::BatteryMgr *_batteryMgr;
        const char *_version;
        statemanagement::StateManager *_stateManager;
        unsigned long *_loopTime;
        bool _connected;
    private:
    };

}
