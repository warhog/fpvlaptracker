#include "btcomm.h"

using namespace comm;

//#define DEBUG

BtComm::BtComm(BluetoothSerial *btSerial, util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808,
    lap::LapDetector *lapDetector, battery::BatteryMgr *batteryMgr, const char *version,
    statemanagement::StateManager *stateManager, comm::WifiComm *wifiComm, unsigned long *loopTime) : 
    Comm(storage, rssi, rx5808, lapDetector, batteryMgr, version, stateManager, loopTime), _serialGotLine(false),
    _serialString(false), _btSerial(btSerial), _wifiComm(wifiComm), _jsonDocument(1024) {
        this->_version = version;
}

int BtComm::connect() {
#ifdef DEBUG
    Serial.println(F("bluetooth connect()"));
#endif
    String name = "FLT" + comm::CommTools::getChipIdAsString();
    if (!this->_btSerial->begin(name)) {
        return btErrorCode::INIT_FAILED;
    }

#ifdef DEBUG
    Serial.print(F("bluetooth name: "));
    Serial.println(name);
#endif
    this->_connected = true;
    return btErrorCode::OK;
}

void BtComm::reg() {
    
}

void BtComm::lap(unsigned long lapTime, unsigned int rssi) {
    this->prepareJson();
    this->_jsonDocument["type"] = "lap";
    this->_jsonDocument["lapTime"] = lapTime;
    this->_jsonDocument["rssi"] = rssi;
    this->sendJson();
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
void BtComm::processIncomingMessage() {
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
        Serial.printf("processIncommingMessage(): %s\n", this->_serialString.c_str());
#endif
        if (this->_serialString.substring(0, 1) == "0") {
            // sometimes on first connect there i a leading zero sent
            this->_serialString = this->_serialString.substring(1, this->_serialString.length() - 1);
        }

        if (this->_serialString.length() >= 11 && this->_serialString.substring(0, 11) == "GET version") {
            // get the version
            this->prepareJson();
            this->_jsonDocument["type"] = "version";
            this->_jsonDocument["version"] = this->_version;
            this->sendJson();
        } else if (this->_serialString.length() >= 8 && this->_serialString.substring(0, 8) == "GET rssi") {
            // get the current rssi
            this->sendRssiData(this->_rssi->getRssi());
        } else if (this->_serialString.length() >= 6 && this->_serialString.substring(0, 6) == "REBOOT") {
            // reboot the device
#ifdef DEBUG
            Serial.println(F("reboot device"));
            Serial.flush();
#endif
            this->disconnect();
            if (this->_wifiComm->isConnected()) {
                this->_wifiComm->disconnect();
            }
            ESP.restart();
        } else if (this->_serialString.length() >= 10 && this->_serialString.substring(0, 10) == "GET device") {
            // get the current config data
            this->processGetDeviceData();
        } else if (this->_serialString.length() >= 11 && this->_serialString.substring(0, 11) == "PUT config ") {
            // store the given config data
            this->processStoreConfig();
        } else if (this->_serialString.length() >= 10 && this->_serialString.substring(0, 10) == "START scan") {
            // start channel scan
            this->_rx5808->startScan(this->_storage->getFrequency());
            this->_stateManager->update(statemanagement::state_enum::SCAN);
            this->sendGenericState("scan", "started");
        } else if (this->_serialString.length() >= 14 && this->_serialString.substring(0, 14) == "START deepscan") {
            // start deep channel scan
            this->_rx5808->startScan(this->_storage->getFrequency());
            this->_stateManager->update(statemanagement::state_enum::DEEPSCAN);
            this->sendGenericState("scan", "started");
        } else if (this->_serialString.length() >= 9 && this->_serialString.substring(0, 9) == "STOP scan") {
            // stop channel scan
            this->_rx5808->stopScan();
            this->_stateManager->update(statemanagement::state_enum::RESTORE_STATE);
            this->sendGenericState("scan", "stopped");
        } else {
            String cmd = F("UNKNOWN_COMMAND: ");
            cmd += this->_serialString;
            this->sendBtMessageWithNewline(cmd);
        }
        this->_serialGotLine = false;
        this->_serialString = "";
    }
}

/*---------------------------------------------------
 * received get device data message
 *-------------------------------------------------*/
void BtComm::processGetDeviceData() {
    this->sendBtMessageWithNewline(comm::CommTools::getDeviceDataAsJsonStringFromStorage(this->_storage, this->_stateManager, this->_lapDetector, this->_batteryMgr, *this->_loopTime, this->_rssi, this->_version));
}

/*---------------------------------------------------
 * received store config message
 *-------------------------------------------------*/
void BtComm::processStoreConfig() {
    // received config
    DeserializationError error = deserializeJson(this->_jsonDocument, this->_serialString.substring(11));
    if (error) {
#ifdef DEBUG
        Serial.println(F("failed to parse config"));
#endif
        this->sendBtMessageWithNewline(F("SETCONFIG: NOK"));
    } else {
#ifdef DEBUG
        Serial.printf("got config: %s\n", this->_serialString.substring(11).c_str());
#endif
        bool reboot = false;
        if (this->_storage->getMinLapTime() != this->_jsonDocument["minimumLapTime"]) {
            this->_storage->setMinLapTime(this->_jsonDocument["minimumLapTime"]);
        }
        
        if (this->_storage->getSsid() != this->_jsonDocument["ssid"]) {
            reboot = true;
            this->_storage->setSsid(this->_jsonDocument["ssid"]);
        }

        if (this->_storage->getWifiPassword() != this->_jsonDocument["password"]) {
            reboot = true;
            this->_storage->setWifiPassword(this->_jsonDocument["password"]);
        }

        if (this->_storage->getFrequency() != this->_jsonDocument["frequency"]) {
            reboot = true;
            this->_storage->setFrequency(this->_jsonDocument["frequency"]);
        }

        if (this->_storage->getTriggerThreshold() != this->_jsonDocument["triggerThreshold"]) {
            reboot = true;
            this->_storage->setTriggerThreshold(this->_jsonDocument["triggerThreshold"]);
        }

        if (this->_storage->getTriggerThresholdCalibration() != this->_jsonDocument["triggerThresholdCalibration"]) {
            reboot = true;
            this->_storage->setTriggerThresholdCalibration(this->_jsonDocument["triggerThresholdCalibration"]);
        }

        if (this->_storage->getCalibrationOffset() != this->_jsonDocument["calibrationOffset"]) {
            this->_storage->setCalibrationOffset(this->_jsonDocument["calibrationOffset"]);
        }
        
        if (this->_storage->getDefaultVref() != this->_jsonDocument["defaultVref"]) {
            reboot = true;
            this->_storage->setDefaultVref(this->_jsonDocument["defaultVref"]);
        }

        if (this->_storage->getFilterRatio() != this->_jsonDocument["filterRatio"]) {
            this->_storage->setFilterRatio(this->_jsonDocument["filterRatio"]);
        }
        
        if (this->_storage->getFilterRatioCalibration() != this->_jsonDocument["filterRatioCalibration"]) {
            this->_storage->setFilterRatioCalibration(this->_jsonDocument["filterRatioCalibration"]);
        }

        if (this->_stateManager->isStateCalibration()) {
            this->_rssi->setFilterRatio(this->_jsonDocument["filterRatioCalibration"]);
        } else {
            this->_rssi->setFilterRatio(this->_jsonDocument["filterRatio"]);
        }

        // allow setting the trigger value outside of calibration mode
        if (!this->_lapDetector->isCalibrating() && this->_jsonDocument["triggerValue"] != this->_lapDetector->getTriggerValue()) {
#ifdef DEBUG
            Serial.printf("setting new trigger value: %d\n", this->_jsonDocument["triggerValue"]);
#endif
            this->_lapDetector->setTriggerValue(this->_jsonDocument["triggerValue"]);
        }

        this->_storage->store();
        this->_lapDetector->init();
        
        String response = F("SETCONFIG: OK");
        if (reboot) {
            response += " reboot";
        }
        this->sendBtMessageWithNewline(response);
    }
}

void BtComm::sendScanData(unsigned int frequency, unsigned int rssi) {
    this->prepareJson();
    this->_jsonDocument["type"] = "scan";
    this->_jsonDocument["frequency"] = frequency;
    this->_jsonDocument["rssi"] = rssi;
    this->sendJson();
}

void BtComm::sendRssiData(unsigned int rssi) {
    this->_jsonDocument["type"] = "rssi";
    this->_jsonDocument["rssi"] = rssi;
    this->sendJson();
}

void BtComm::sendGenericState(const char* type, const char* state) {
    this->prepareJson();
    this->_jsonDocument["type"] = "state";
    this->_jsonDocument[type] = state;
    this->sendJson();
}

void BtComm::sendCalibrationDone() {
    this->sendGenericState("calibration", "done");
}

void BtComm::prepareJson() {
    this->_jsonDocument.clear();
}

void BtComm::sendVoltageAlarm(double voltage) {
    this->prepareJson();
    this->_jsonDocument["type"] = "alarm";
    this->_jsonDocument["voltage"] = voltage;
    this->_jsonDocument["msg"] = "Battery voltage low!";
    this->sendJson();
}

void BtComm::sendJson() {
    String result("");
    serializeJson(this->_jsonDocument, result);
    this->sendBtMessageWithNewline(result);
}

bool BtComm::hasClient() {
    return this->_btSerial->hasClient();
}

void BtComm::disconnect() {
    this->_connected = false;
    this->_btSerial->end();
}