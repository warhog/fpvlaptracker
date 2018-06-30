#pragma once

#include <Arduino.h>
#include <ArduinoJson.h>

#include "comm.h"
#include "storage.h"
#include "rssi.h"
#include "frequency.h"

namespace comm {

    enum btErrorCode { OK = 0, MODULE_NOT_RESPONDING = -1, NAME_COMMAND_FAILED = -2, PIN_COMMAND_FAILED = -3 };

    class BtComm : public Comm {
    public:
        BtComm(util::Storage *storage, lap::Rssi *rssi);
        void reg();
        void lap(unsigned long lapTime, unsigned int rssi);
        int connect();
        void processIncommingMessage();
        void setState(String state);
        
    private:
        bool btSendAndWaitForOK(String data);
        void sendBtMessage(String msg);
        void sendBtMessage(String msg, boolean newLine);
        void sendBtMessageWithNewline(String msg);
        void processGetConfig();
        void processStoreConfig();
        lap::Rssi *_rssi;
        bool _serialGotLine;
        String _serialString;
        String _state;
    };

}