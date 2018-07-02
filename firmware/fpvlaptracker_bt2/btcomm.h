#pragma once

#include <Arduino.h>
#include <ArduinoJson.h>

#include "comm.h"
#include "storage.h"
#include "rssi.h"
#include "frequency.h"
#include "rx5808.h"

namespace comm {

    enum btErrorCode { OK = 0, MODULE_NOT_RESPONDING = -1, NAME_COMMAND_FAILED = -2, PIN_COMMAND_FAILED = -3 };

    class BtComm : public Comm {
    public:
        BtComm(util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808);
        void reg();
        void lap(unsigned long lapTime, unsigned int rssi);
        int connect();
        void processIncommingMessage();
        void setState(String state);
        void sendScanData(unsigned int frequency, unsigned int rssi);
        
    private:
        bool btSendAndWaitForOK(String data);
        void sendBtMessage(String msg);
        void sendBtMessage(String msg, boolean newLine);
        void sendBtMessageWithNewline(String msg);
        void processGetConfig();
        void processStoreConfig();
        lap::Rssi *_rssi;
        radio::Rx5808 *_rx5808;
        bool _serialGotLine;
        String _serialString;
        String _state;
    };

}