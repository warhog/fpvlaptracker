#pragma once

#include <Arduino.h>
#include <ArduinoJson.h>
#include "storage.h"
#include "statemanager.h"
#include "lapdetector.h"
#include "batterymgr.h"
#include "rssi.h"

namespace comm {

    class CommTools {
        public:
            static String getDeviceDataAsJsonStringFromStorage(util::Storage *storage, statemanagement::StateManager *stateManager, lap::LapDetector *lapDetector, battery::BatteryMgr *batteryMgr, unsigned long loopTime, lap::Rssi *rssi, const char *version);
            static String getChipIdAsString();

        private:
    };

}