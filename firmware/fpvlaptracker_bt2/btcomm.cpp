#include "btcomm.h"

using namespace comm;

//#define DEBUG

BtComm::BtComm(util::Storage *storage, lap::Rssi *rssi) : Comm(storage), _serialGotLine(false), _serialString(false),
    _rssi(rssi) {

}


int BtComm::connect() {
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
            // TODO currently the hc06 is not responding after a reboot
            ESP.reset();
        } else if (this->_serialString.length() >= 10 && this->_serialString.substring(0, 10) == "GET config") {
            // get the current config data
            // TODO
        } else if (this->_serialString.length() >= 11 && this->_serialString.substring(0, 11) == "PUT config ") {
            // store the given config data
            // TODO
        } else if (this->_serialString.length() >= 12 && this->_serialString.substring(0, 12) == "GET channels") {
            // scan all channels
            //TODO processScanChannels();
        } else {
            this->sendBtMessageWithNewline(F("UNKNOWN_COMMAND"));
        }
        this->_serialGotLine = false;
        this->_serialString = "";
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

