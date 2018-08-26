#include "btcomm.h"

using namespace comm;

//#define DEBUG

BtComm::BtComm(BluetoothSerial *btSerial, util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808,
    lap::LapDetector *lapDetector, battery::BatteryMgr *batteryMgr, const char *version,
    statemanagement::StateManager *stateManager) : Comm(storage), _serialGotLine(false),
    _serialString(false), _rssi(rssi), _rx5808(rx5808), _btSerial(btSerial), _lapDetector(lapDetector),
    _jsonBuffer(300), _batteryMgr(batteryMgr), _version(version), _stateManager(stateManager) {

}

int BtComm::connect() {
#ifdef DEBUG
    Serial.println(F("bluetooth connect()"));
#endif
    uint64_t chipId = ESP.getEfuseMac();
    char strChipId[15] = { 0 };
    sprintf(strChipId, "%u", chipId);
    String chipString = strChipId;
    String name = "FLT" + chipString;
    this->_btSerial->begin(name);

#ifdef DEBUG
    Serial.print(F("name command: "));
    Serial.println(name);
#endif
    this->_connected = true;
    return btErrorCode::OK;
}

void BtComm::reg() {
    
}

void BtComm::lap(unsigned long lapTime, unsigned int rssi) {
    JsonObject& root = this->prepareJson();
    root["type"] = "lap";
    root["lapTime"] = lapTime;
    root["rssi"] = rssi;
    this->sendJson(root);
}

void BtComm::sendBtMessage(String msg) {
    sendBtMessage(msg, false);
}

void BtComm::sendBtMessageWithNewline(String msg) {
    sendBtMessage(msg, true);
}
void BtComm::sendBtMessage(String msg, boolean newLine) {
    if (newLine) {
        msg += '\n';
    }
#ifdef DEBUG
    Serial.print(F("sendBtMessage(): "));
    Serial.println(msg);
#endif
    this->_btSerial->print(msg.c_str());
}

/*---------------------------------------------------
 * event handler for incoming serial events (hc-06)
 *-------------------------------------------------*/
void BtComm::processIncommingMessage() {
    // process serial data
    while (this->_btSerial->available()) {
        char c = (char)this->_btSerial->read();
        this->_serialString += c;
        if (c == '\n') {
            this->_serialGotLine = true;
        }
    }

    // process serial line
    if (this->_serialGotLine) {
#ifdef DEBUG
        Serial.print(F("processIncommingMessage(): "));
        Serial.println(this->_serialString);
#endif
        if (this->_serialString.substring(0, 1) == "0") {
            // sometimes on first connect there i a leading zero sent
            this->_serialString = this->_serialString.substring(1, this->_serialString.length() - 1);
        }

        if (this->_serialString.length() >= 11 && this->_serialString.substring(0, 11) == "GET version") {
            // get the version
            JsonObject& root = this->prepareJson();
            root["type"] = "version";
            root["version"] = "FLT32-R1.0";
            this->sendJson(root);
        } else if (this->_serialString.length() >= 8 && this->_serialString.substring(0, 8) == "GET rssi") {
            // get the current rssi
            this->sendFastRssiData(this->_rssi->getRssi());
        } else if (this->_serialString.length() >= 6 && this->_serialString.substring(0, 6) == "REBOOT") {
            // reboot the device
            ESP.restart();
        } else if (this->_serialString.length() >= 9 && this->_serialString.substring(0, 9) == "GET state") {
            // get the current state
            this->sendGenericState("state", this->_state.c_str());
        } else if (this->_serialString.length() >= 10 && this->_serialString.substring(0, 10) == "GET config") {
            // get the current config data
            this->processGetConfig();
        } else if (this->_serialString.length() >= 11 && this->_serialString.substring(0, 11) == "PUT config ") {
            // store the given config data
            this->processStoreConfig();
        } else if (this->_serialString.length() >= 15 && this->_serialString.substring(0, 15) == "GET runtimedata") {
            // get runtime data
            this->processGetRuntimeData();
        } else if (this->_serialString.length() >= 10 && this->_serialString.substring(0, 10) == "START scan") {
            // start channel scan
            this->_rx5808->startScan(this->_storage->getChannelIndex());
            this->_stateManager->update(statemanagement::state_enum::SCAN);
            this->sendGenericState("scan", "started");
        } else if (this->_serialString.length() >= 9 && this->_serialString.substring(0, 9) == "STOP scan") {
            // stop channel scan
            this->_rx5808->stopScan();
            this->_stateManager->update(statemanagement::state_enum::RESTORE_STATE);
            this->sendGenericState("scan", "stopped");
        } else if (this->_serialString.length() >= 10 && this->_serialString.substring(0, 10) == "START rssi") {
            // start fast rssi scan
            this->_stateManager->update(statemanagement::state_enum::RSSI);
            this->sendGenericState("rssi", "started");
        } else if (this->_serialString.length() >= 9 && this->_serialString.substring(0, 9) == "STOP rssi") {
            // stop fast rssi scan
            this->_stateManager->update(statemanagement::state_enum::RESTORE_STATE);
            this->sendGenericState("rssi", "stopped");
        } else {
            String cmd = F("UNKNOWN_COMMAND: ");
            cmd += this->_serialString;
            this->sendBtMessageWithNewline(cmd);
        }
        this->_serialGotLine = false;
        this->_serialString = "";
    }
}

void BtComm::processGetRuntimeData() {
    JsonObject& root = this->prepareJson();
    root["type"] = "runtime";
    root["triggerValue"] = this->_lapDetector->getTriggerValue();
    this->sendJson(root);
}

/*---------------------------------------------------
 * received get config message
 *-------------------------------------------------*/
void BtComm::processGetConfig() {
    JsonObject& root = this->prepareJson();
    root["type"] = "config";
    root["frequency"] = freq::Frequency::getFrequencyForChannelIndex(this->_storage->getChannelIndex());
    root["minimumLapTime"] = this->_storage->getMinLapTime();
    root["ssid"] = this->_storage->getSsid();
    root["password"] = this->_storage->getWifiPassword();
    root["triggerThreshold"] = this->_storage->getTriggerThreshold();
    root["triggerThresholdCalibration"] = this->_storage->getTriggerThresholdCalibration();
    root["calibrationOffset"] = this->_storage->getCalibrationOffset();
    root["state"] = this->_state;
    root["triggerValue"] = this->_lapDetector->getTriggerValue();
    root["voltage"] = this->_batteryMgr->getVoltage();
    root["uptime"] = millis() / 1000;
    this->sendJson(root);
}

/*---------------------------------------------------
 * received store config message
 *-------------------------------------------------*/
void BtComm::processStoreConfig() {
  // received config
  DynamicJsonBuffer jsonBuffer(200);
  JsonObject& root = jsonBuffer.parseObject(this->_serialString.substring(11));
  if (!root.success()) {
#ifdef DEBUG
    Serial.println(F("failed to parse config"));
#endif
    this->sendBtMessageWithNewline(F("SETCONFIG: NOK"));
  } else {
    this->_storage->setMinLapTime(root["minimumLapTime"]);
    this->_storage->setSsid(root["ssid"]);
    this->_storage->setWifiPassword(root["password"]);
    this->_storage->setChannelIndex(freq::Frequency::getChannelIndexForFrequency(root["frequency"]));
    this->_storage->setTriggerThreshold(root["triggerThreshold"]);
    this->_storage->setTriggerThresholdCalibration(root["triggerThresholdCalibration"]);
    this->_storage->setCalibrationOffset(root["calibrationOffset"]);
    this->_storage->store();
    this->sendBtMessageWithNewline(F("SETCONFIG: OK"));
  }
}

void BtComm::setState(String state) {
    this->_state = state;
}

void BtComm::sendScanData(unsigned int frequency, unsigned int rssi) {
    JsonObject& root = this->prepareJson();
    root["type"] = "scan";
    root["frequency"] = frequency;
    root["rssi"] = rssi;
    this->sendJson(root);
}

void BtComm::sendFastRssiData(unsigned int rssi) {
    JsonObject& root = this->prepareJson();
    root["type"] = "rssi";
    root["rssi"] = rssi;
    this->sendJson(root);
}

void BtComm::sendGenericState(const char* type, const char* state) {
    JsonObject& root = this->prepareJson();
    root["type"] = "state";
    root[type] = state;
    this->sendJson(root);
}

void BtComm::sendCalibrationDone() {
    this->sendGenericState("calibration", "done");
}

JsonObject& BtComm::prepareJson() {
    this->_jsonBuffer.clear();
    JsonObject& root = this->_jsonBuffer.createObject();
    return root;
}

void BtComm::sendVoltageAlarm() {
    JsonObject& root = this->prepareJson();
    root["type"] = "alarm";
    root["msg"] = "Battery voltage low!";
    this->sendJson(root);
}

void BtComm::sendJson(JsonObject& root) {
    String result("");
    root.printTo(result);
    this->sendBtMessageWithNewline(result);
}

bool BtComm::hasClient() {
    return this->_btSerial->hasClient();
}