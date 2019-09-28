#pragma once

#include <Arduino.h>
#include <WiFi.h>
#include <ArduinoJson.h>

#include "commtools.h"
#include "comm.h"
#include "storage.h"
#include "rssi.h"
#include "frequency.h"
#include "rx5808.h"
#include "statemanager.h"
#include "lapdetector.h"
#include "batterymgr.h"
#include "version.h"

namespace comm {

    class WifiComm : public Comm {
    public:
        WifiComm(util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808, lap::LapDetector *lapDetector, battery::BatteryMgr *batteryMgr, statemanagement::StateManager *stateManager, unsigned long *loopTime);
        void reg();
        void lap(unsigned long duration, unsigned int rssi);
        int connect();
        void processIncomingMessage();
        void sendUdpUnicastToServerMessage(String msg);
        void sendUdpBroadcastMessage(String msg);
        void sendCalibrationDone();
        void sendData();
        void disconnect();
        void sendVoltageAlarm(double voltage);
        void sendRssiData(unsigned int rssi);
        void sendScanData(unsigned int frequency, unsigned int rssi);

    private:
        String getBroadcastIP();
        String generateJsonString();
        WiFiUDP _udp;
        bool _wifiSsidFound;
        DynamicJsonDocument _jsonDocument;
        String _serverIp;
    };

}