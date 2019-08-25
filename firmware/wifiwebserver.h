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

namespace comm {
    class WifiWebServer {
        public:
            WifiWebServer(util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808, lap::LapDetector *lapDetector,
                battery::BatteryMgr *batteryMgr, const char *version, statemanagement::StateManager *stateManager,
                unsigned long *loopTime) :
                _storage(storage), _rssi(rssi), _rx5808(rx5808), _lapDetector(lapDetector), _batteryMgr(batteryMgr),
                _version(version), _stateManager(stateManager), _loopTime(loopTime), _jsonDocument(1024) {}
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

            WebServer _server;
            util::Storage *_storage;
            lap::Rssi *_rssi;
            radio::Rx5808 *_rx5808;
            lap::LapDetector *_lapDetector;
            battery::BatteryMgr *_batteryMgr;
            statemanagement::StateManager *_stateManager;
            DynamicJsonDocument _jsonDocument;
            const char *_version;
            bool _connected;
            unsigned long *_loopTime;
            char const *_header = "<html><head><style>body { font-family: Arial; background: #E6E6E6; } a { color: #0000ff; } #content { margin: auto; border-radius: 5px; background: #ffffff; padding: 20px; width: 600px;} #overlay { position: fixed; display: none; width: 100%; height: 100%; top: 0; left: 0; right: 0; bottom: 0; background-color: rgba(0,0,0,0.5); z-index: 2; cursor:wait;}</style><script>function overlay() { document.getElementById('overlay').style.display = 'block'; }</script></head><body><div id='overlay'></div><div id='content'><h1>fpvlaptracker32</h1>";
            char const *_footer = "</div></body></html>";
            char const *_serverIndex = "chip id: %CHIPID%<br />current version: %VERSION%<br />build date: " __DATE__ "  " __TIME__ "<br /><br /><a href='/bluetooth'>switch to bluetooth</a><br /><hr size='1'/><h2>maintenance</h2>select .bin file to flash.<br /><br /><form method='POST' action='/update' enctype='multipart/form-data'><input type='file' name='update'><input type='submit' value='update' onclick='overlay();'></form><br /><a href='/factorydefaults'>load factory defaults</a><br /><br /><a href='/vref'>output VREF</a>";
    };
}