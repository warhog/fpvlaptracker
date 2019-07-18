#pragma once

#include <Arduino.h>
#include <WiFi.h>

namespace comm {

    class WifiAp {
    public:
        void connect();
        bool isConnected() const {
            return this->_connected;
        }
        void disconnect();
    private:
        bool _connected;
    };

}