#pragma once

#include <Arduino.h>
#include "storage.h"

namespace comm {

    class Comm {
    public:
        Comm(util::Storage *storage);
        void reg();
        void lap();
        int connect();
        void storeConfig();
        void loadConfig();
        bool isConnected() const {
            return this->_connected;
        }
    protected:
        util::Storage *_storage;
        bool _connected;
    private:
    };

}
