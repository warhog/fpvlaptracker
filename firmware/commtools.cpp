#include "commtools.h"

using namespace comm;

String CommTools::getDeviceDataAsJsonStringFromStorage(util::Storage *storage, statemanagement::StateManager *stateManager, lap::LapDetector *lapDetector, battery::BatteryMgr *batteryMgr, unsigned long loopTime, lap::Rssi *rssi, const char *version) {
    DynamicJsonDocument jsonDocument(1024);
    unsigned long chipId = static_cast<unsigned long>(ESP.getEfuseMac());
    jsonDocument["type"] = "devicedata";
    jsonDocument["chipid"] = chipId;
    jsonDocument["frequency"] = storage->getFrequency();
    jsonDocument["minimumLapTime"] = storage->getMinLapTime();
    jsonDocument["triggerThreshold"] = storage->getTriggerThreshold();
    jsonDocument["triggerThresholdCalibration"] = storage->getTriggerThresholdCalibration();
    jsonDocument["calibrationOffset"] = storage->getCalibrationOffset();
    jsonDocument["state"] = stateManager->toString(stateManager->getState());
    jsonDocument["triggerValue"] = lapDetector->getTriggerValue();
    jsonDocument["voltage"] = batteryMgr->getVoltage();
    jsonDocument["uptime"] = millis() / 1000;
    jsonDocument["defaultVref"] = storage->getDefaultVref();
    jsonDocument["rssi"] = rssi->getRssi();
    jsonDocument["loopTime"] = loopTime;
    jsonDocument["filterRatio"] = storage->getFilterRatio();
    jsonDocument["filterRatioCalibration"] = storage->getFilterRatioCalibration();
    jsonDocument["version"] = version;
    jsonDocument["ssid"] = storage->getSsid();
    jsonDocument["password"] = storage->getWifiPassword();
    String result("");
    serializeJson(jsonDocument, result);
    return result;

}

String CommTools::getChipIdAsString() {
    unsigned long chipIdLong = static_cast<unsigned long>(ESP.getEfuseMac());
//    String chipIdString = chipIdLong;
    return String(chipIdLong);
    // uint64_t chipId = ESP.getEfuseMac();
    // char strChipId[15] = { 0 };
    // sprintf(strChipId, "%u", chipId);
    // String chipString = strChipId;

}