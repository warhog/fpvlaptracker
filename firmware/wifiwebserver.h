#pragma once

#include <Arduino.h>
#include <WebServer.h>
#include <ArduinoJson.h>
#include <Update.h>

#include "storage.h"
#include "rssi.h"
#include "frequency.h"
#include "rx5808.h"
#include "statemanager.h"
#include "lapdetector.h"
#include "batterymgr.h"
#include "commtools.h"
#include "version.h"
#include "webui-generated.h"

namespace comm {
    class WifiWebServer {
        public:
            WifiWebServer(util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808, lap::LapDetector *lapDetector,
                battery::BatteryMgr *batteryMgr, statemanagement::StateManager *stateManager,
                unsigned long *loopTime) :
                _storage(storage), _rssi(rssi), _rx5808(rx5808), _lapDetector(lapDetector), _batteryMgr(batteryMgr),
                _stateManager(stateManager), _loopTime(loopTime), _jsonDocument(1024) {}
            void begin();
            void handle();
            bool isConnected() {
                return this->_connected;
            }
            void disconnect() {
                this->_connected = false;
                this->_server.stop();
            }
        private:
            void prepareJson();
            void sendJson();
            String concat(String text);
            void disconnectClients();
            void rebootNode();

            WebServer _server;
            util::Storage *_storage;
            lap::Rssi *_rssi;
            radio::Rx5808 *_rx5808;
            lap::LapDetector *_lapDetector;
            battery::BatteryMgr *_batteryMgr;
            statemanagement::StateManager *_stateManager;
            DynamicJsonDocument _jsonDocument;
            bool _connected;
            unsigned long *_loopTime;
    };
}
