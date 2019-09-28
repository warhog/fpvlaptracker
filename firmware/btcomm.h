#pragma once

#include <Arduino.h>
#include <ArduinoJson.h>
#include <BluetoothSerial.h>

#include "commtools.h"
#include "comm.h"
#include "storage.h"
#include "rssi.h"
#include "frequency.h"
#include "rx5808.h"
#include "statemanager.h"
#include "lapdetector.h"
#include "batterymgr.h"
#include "wificomm.h"
#include "version.h"

namespace comm {

    enum btErrorCode { OK = 0, INIT_FAILED = -1 };

    class BtComm : public Comm {
    public:
        BtComm(BluetoothSerial *btSerial, util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808, lap::LapDetector *lapDetector, battery::BatteryMgr *batteryMgr, statemanagement::StateManager *stateManager, comm::WifiComm *wifiComm, unsigned long *loopTime);
        void reg();
        void lap(unsigned long lapTime, unsigned int rssi);
        int connect();
        void processIncomingMessage();
        void sendScanData(unsigned int frequency, unsigned int rssi);
        void sendRssiData(unsigned int rssi);
        void sendCalibrationDone();
        void sendVoltageAlarm(double voltage);
        void sendGenericState(const char* type, const char* state);
        bool hasClient();
        void disconnect();

    private:
        void sendJson();
        void prepareJson();
        void sendBtMessage(String msg);
        void sendBtMessage(String msg, boolean newLine);
        void sendBtMessageWithNewline(String msg);
        void processGetDeviceData();
        void processStoreConfig();
        BluetoothSerial *_btSerial;
        bool _serialGotLine;
        String _serialString;
        DynamicJsonDocument _jsonDocument;
        comm::WifiComm *_wifiComm;
    };

}