#pragma once

#include <Arduino.h>
#include <ArduinoJson.h>
#include <BluetoothSerial.h>

#include "comm.h"
#include "storage.h"
#include "rssi.h"
#include "frequency.h"
#include "rx5808.h"
#include "publisher.h"
#include "statemanager.h"

namespace comm {

    enum btErrorCode { OK = 0, NAME_COMMAND_FAILED = -1 };

    class BtComm : public Comm, public pubsub::Publisher<statemanagement::state_enum> {
    public:
        BtComm(BluetoothSerial *btSerial, util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808);
        void reg();
        void lap(unsigned long lapTime, unsigned int rssi);
        int connect();
        void processIncommingMessage();
        void setState(String state);
        void sendScanData(unsigned int frequency, unsigned int rssi);
        void sendFastRssiData(unsigned int rssi);
        
    private:
        void sendBtMessage(String msg);
        void sendBtMessage(String msg, boolean newLine);
        void sendBtMessageWithNewline(String msg);
        void processGetConfig();
        void processStoreConfig();
        BluetoothSerial *_btSerial;
        lap::Rssi *_rssi;
        radio::Rx5808 *_rx5808;
        bool _serialGotLine;
        String _serialString;
        String _state;
    };

}