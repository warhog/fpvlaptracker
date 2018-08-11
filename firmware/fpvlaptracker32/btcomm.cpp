#include "btcomm.h"

using namespace comm;

//#define DEBUG

BtComm::BtComm(BluetoothSerial *btSerial, util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808) : Comm(storage), _serialGotLine(false),
    _serialString(false), _rssi(rssi), _rx5808(rx5808), _btSerial(btSerial) {

}

int BtComm::connect() {
#ifdef DEBUG
    Serial.println(F("bluetooth connect()"));
#endif
    uint64_t chipId = ESP.getEfuseMac();
    char strChipId[10];
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
    this->sendBtMessageWithNewline("LAP: " + String(lapTime));
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
        if (this->_serialString.length() >= 11 && this->_serialString.substring(0, 11) == "GET version") {
            // get the version
            String v = F("VERSION: 2.0");
            this->sendBtMessageWithNewline(v);
        } else if (this->_serialString.length() >= 12 && this->_serialString.substring(0, 12) == "0GET version") {
            // TODO workaround for esp32, first connect after start sends a leading 0 all the time
            // get the version
            String v = F("VERSION: 2.0");
            this->sendBtMessageWithNewline(v);
        } else if (this->_serialString.length() >= 8 && this->_serialString.substring(0, 8) == "GET rssi") {
            // get the current rssi
            String r = F("RSSI: ");
            r += this->_rssi->getRssi();
            this->sendBtMessageWithNewline(r);
        } else if (this->_serialString.length() >= 6 && this->_serialString.substring(0, 6) == "REBOOT") {
            // reboot the device
            ESP.restart();
        } else if (this->_serialString.length() >= 9 && this->_serialString.substring(0, 9) == "GET state") {
            // get the current state
            String s = F("STATE: ");
            s += this->_state;
            this->sendBtMessageWithNewline(s);
        } else if (this->_serialString.length() >= 10 && this->_serialString.substring(0, 10) == "GET config") {
            // get the current config data
            this->processGetConfig();
        } else if (this->_serialString.length() >= 11 && this->_serialString.substring(0, 11) == "PUT config ") {
            // store the given config data
            this->processStoreConfig();
        } else if (this->_serialString.length() >= 10 && this->_serialString.substring(0, 10) == "START scan") {
            // start channel scan
            this->_rx5808->startScan(this->_storage->getChannelIndex());
            this->notifySubscribers(statemanagement::state_enum::SCAN);
            this->sendBtMessageWithNewline("SCAN: started");
        } else if (this->_serialString.length() >= 9 && this->_serialString.substring(0, 9) == "STOP scan") {
            // stop channel scan
            this->_rx5808->stopScan();
            this->notifySubscribers(statemanagement::state_enum::RESTORE_STATE);
            this->sendBtMessageWithNewline("SCAN: stopped");
        } else if (this->_serialString.length() >= 10 && this->_serialString.substring(0, 10) == "START rssi") {
            // start fast rssi scan
            this->notifySubscribers(statemanagement::state_enum::RSSI);
            this->sendBtMessageWithNewline("RSSI: started");
        } else if (this->_serialString.length() >= 9 && this->_serialString.substring(0, 9) == "STOP rssi") {
            // stop fast rssi scan
            this->notifySubscribers(statemanagement::state_enum::RESTORE_STATE);
            this->sendBtMessageWithNewline("RSSI: stopped");
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
 * received get config message
 *-------------------------------------------------*/
void BtComm::processGetConfig() {
    DynamicJsonBuffer jsonBuffer(200);
    JsonObject& root = jsonBuffer.createObject();
    root["frequency"] = freq::Frequency::getFrequencyForChannelIndex(this->_storage->getChannelIndex());
    root["minimumLapTime"] = this->_storage->getMinLapTime();
    root["ssid"] = this->_storage->getSsid();
    root["password"] = this->_storage->getWifiPassword();
    root["triggerThreshold"] = this->_storage->getTriggerThreshold();
    root["triggerThresholdCalibration"] = this->_storage->getTriggerThresholdCalibration();
    root["calibrationOffset"] = this->_storage->getCalibrationOffset();
    root["state"] = this->_state;
    String c = F("CONFIG: ");
    root.printTo(c);
    this->sendBtMessageWithNewline(c);
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
    String scan = F("SCAN: ");
    scan += frequency;
    scan += "=";
    scan += rssi;
    this->sendBtMessageWithNewline(scan);
}

void BtComm::sendFastRssiData(unsigned int rssi) {
    String rssi2 = F("RSSI: ");
    rssi2 += rssi;
    this->sendBtMessageWithNewline(rssi2);
}
