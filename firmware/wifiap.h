#pragma once

#include <Arduino.h>
#include <WiFi.h>
#include "commtools.h"

namespace comm {

    class WifiAp {
    public:
        bool connect();
        bool isConnected() const {
            return this->_connected;
        }
        void disconnect();
    private:
        bool _connected;
    };

}