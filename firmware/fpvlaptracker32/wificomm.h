#pragma once

#include <Arduino.h>
#include <WiFi.h>

#include "comm.h"
#include "storage.h"

namespace comm {

    class WifiComm : public Comm {
    public:
        WifiComm(util::Storage *storage);
        void reg();
        void lap(unsigned long lapTime, unsigned int rssi);
        int connect();
        void processIncommingMessage();
        void sendUdpMessage(String msg);

    private:
        String getBroadcastIP();
        WiFiUDP _udp;
        bool _wifiSsidFound;
    };

}