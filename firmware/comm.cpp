#include "comm.h"

using namespace comm;

Comm::Comm(util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808, lap::LapDetector *lapDetector,
    battery::BatteryMgr *batteryMgr, const char *version, statemanagement::StateManager *stateManager,
    unsigned long *loopTime) : _storage(storage), _rssi(rssi), _rx5808(rx5808), _lapDetector(lapDetector),
    _batteryMgr(batteryMgr), _version(version), _stateManager(stateManager), _loopTime(loopTime) {   
}