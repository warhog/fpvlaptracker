#pragma once

#include "storage.h"

class WifiServer {
public:
    WifiServer(Storage *storage);
    
private:

    Storage *_storage;

}