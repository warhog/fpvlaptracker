#pragma once

#include <Arduino.h>
#include <ArduinoJson.h>
#include <BluetoothSerial.h>

#include "comm.h"
#include "storage.h"
#include "rssi.h"
#include "frequency.h"
#include "rx5808.h"
#include "statemanager.h"
#include "lapdetector.h"
#include "batterymgr.h"

namespace comm {

    enum btErrorCode { OK = 0, NAME_COMMAND_FAILED = -1 };

    class BtComm : public Comm {
    public:
        BtComm(BluetoothSerial *btSerial, util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808, lap::LapDetector *lapDetector, battery::BatteryMgr *batteryMgr, const char *version, statemanagement::StateManager *stateManager);
        void reg();
        void lap(unsigned long lapTime, unsigned int rssi);
        int connect();
        void processIncommingMessage();
        void setState(String state);
        void sendScanData(unsigned int frequency, unsigned int rssi);
        void sendFastRssiData(unsigned int rssi);
        void sendCalibrationDone();
        void sendVoltageAlarm();
        void sendGenericState(const char* type, const char* state);
        bool hasClient();

    private:
        void sendJson(JsonObject& root);
        JsonObject& prepareJson();
        void sendBtMessage(String msg);
        void sendBtMessage(String msg, boolean newLine);
        void sendBtMessageWithNewline(String msg);
        void processGetConfig();
        void processStoreConfig();
        void processGetRuntimeData();
        BluetoothSerial *_btSerial;
        lap::Rssi *_rssi;
        radio::Rx5808 *_rx5808;
        lap::LapDetector *_lapDetector;
        battery::BatteryMgr *_batteryMgr;
        bool _serialGotLine;
        String _serialString;
        String _state;
        DynamicJsonBuffer _jsonBuffer;
        const char *_version;
        statemanagement::StateManager *_stateManager;
    };

}