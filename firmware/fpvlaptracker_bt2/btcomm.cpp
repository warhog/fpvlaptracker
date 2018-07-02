#include "btcomm.h"

using namespace comm;

//#define DEBUG

BtComm::BtComm(util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808) : Comm(storage), _serialGotLine(false), _serialString(false),
    _rssi(rssi), _rx5808(rx5808) {

}

extern "C" {
    #include "user_interface.h"
    extern struct rst_info resetInfo;
}

int BtComm::connect() {

    if (resetInfo.reason == REASON_SOFT_RESTART) {
        // do not initialize bt module on soft restarts, it is already initialized from previous init
        this->_connected = true;
        return btErrorCode::OK;
    }

    unsigned long chipId = ESP.getChipId();
    char strChipId[10];
    sprintf(strChipId, "%u", chipId);
    String chipString = strChipId;
    String name = "AT+NAMEFLT" + chipString;
    String pin = "AT+PIN" + chipString.substring(0, 4);

#ifdef DEBUG
    Serial.print(F("name command: "));
    Serial.println(name);
    Serial.print(F("pin command: "));
    Serial.println(pin);
#else
    unsigned int count = 5;
    while (count > 0) {
        count--;
        if (btSendAndWaitForOK(F("AT+VERSION"))) {
            break;
        }   
    }
    if  (count == 0) {
        return btErrorCode::MODULE_NOT_RESPONDING;
    }
    if (!btSendAndWaitForOK(name)) {
        return btErrorCode::NAME_COMMAND_FAILED;
    }
    if (!btSendAndWaitForOK(pin)) {
        return btErrorCode::PIN_COMMAND_FAILED;
    }
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
#ifndef DEBUG
    if (newLine) {
        msg += '\n';
    }
    Serial.write(msg.c_str());
#endif
}

/*---------------------------------------------------
 * event handler for incoming serial events (hc-06)
 *-------------------------------------------------*/
void BtComm::processIncommingMessage() {
    // process serial data
    while (Serial.available()) {
        char c = (char)Serial.read();
        this->_serialString += c;
        if (c == '\n') {
            this->_serialGotLine = true;
        }
    }

    // process serial line
    if (this->_serialGotLine) {
        if (this->_serialString.length() >= 11 && this->_serialString.substring(0, 11) == "GET version") {
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
        } else if (this->_serialString.length() >= 10 && this->_serialString.substring(0, 10) == "SCAN start") {
            // start channel scan
            this->_rx5808->startScan(this->_storage->getChannelIndex());
            this->sendBtMessageWithNewline("SCAN: started");
        } else if (this->_serialString.length() >= 9 && this->_serialString.substring(0, 9) == "SCAN stop") {
            // stop channel scan
            this->_rx5808->stopScan();
            this->sendBtMessageWithNewline("SCAN: stopped");
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

/*---------------------------------------------------
 * serial config method for hc-06
 *-------------------------------------------------*/
bool BtComm::btSendAndWaitForOK(String data) {
    this->sendBtMessage(data);
    unsigned int waitProtection = 500;
    while (Serial.available() == 0 && waitProtection > 0) {
        delay(10);
        waitProtection--;
    }
    if (waitProtection == 0) {
#ifdef DEBUG
        Serial.println(F("failed waiting for input"));
#endif
        return false;
    }
    // put short delay, hc06 takes some time for sending the config response
    delay(500);

    String result = "";
    while (Serial.available() > 0) {
        char c = (char)Serial.read();
        result += c;
    }

    if (result.substring(0, 2) == "OK") {
        return true;
    }
#ifdef DEBUG
    Serial.println(F("NOK"));
#endif
    return false;
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