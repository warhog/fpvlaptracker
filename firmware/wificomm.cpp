#include "wificomm.h"

using namespace comm;

//#define DEBUG
const unsigned int UDP_PORT = 31337;

WifiComm::WifiComm(util::Storage *storage, lap::Rssi *rssi, radio::Rx5808 *rx5808, lap::LapDetector *lapDetector,
    battery::BatteryMgr *batteryMgr, const char *version, statemanagement::StateManager *stateManager,
    unsigned long *loopTime) :
    Comm(storage, rssi, rx5808, lapDetector, batteryMgr, version, stateManager, loopTime), _wifiSsidFound(false), _jsonDocument(1024), _serverIp("") {
        this->_version = version;
}

int WifiComm::connect() {
    this->_wifiSsidFound = false;
	this->_connected = false;

	int nrOfWifis = WiFi.scanNetworks(false, false, false, 1000);
#ifdef DEBUG
	Serial.print(nrOfWifis);
	Serial.println(F(" network(s) found"));
#endif
	for (unsigned int i = 0; i < nrOfWifis; i++) {
#ifdef DEBUG
		Serial.println(WiFi.SSID(i));
#endif
		if (WiFi.SSID(i) == this->_storage->getSsid()) {
			this->_wifiSsidFound = true;
#ifdef DEBUG
			Serial.println(F("found ssid, connecting"));
#endif
		}
	}

	if (this->_wifiSsidFound) {
        unsigned int tries = 5;
        while (tries > 0) {
#ifdef DEBUG
            Serial.print(F("wifi connecting to ssid "));
            Serial.print(this->_storage->getSsid());
#endif
    		WiFi.begin(this->_storage->getSsid().c_str(), this->_storage->getWifiPassword().c_str());
            unsigned int wait = 0;
            while (WiFi.status() != WL_CONNECTED && wait < 10000) {
                delay(500);
                wait += 500;
#ifdef DEBUG
    			Serial.print(F("."));
#endif
            }
            tries--;
            if (WiFi.status() != WL_CONNECTED) {
                WiFi.disconnect();
#ifdef DEBUG
                if (tries == 0) {
        			Serial.println(F("cannot connect to ssid, switching to standalone mode"));
                } else {
                    Serial.println(F("cannot connect to ssid, retrying"));
                }
#endif
		    } else if (WiFi.status() == WL_CONNECTED) {
#ifdef DEBUG
    			Serial.println(F("connected"));
#endif
    			this->_connected = true;
                tries = 0;
            }

        }
        if (this->_connected) {
#ifdef DEBUG
            Serial.println(F("WiFi set up"));
            Serial.print(F("IP address: "));
            Serial.println(WiFi.localIP());
            Serial.print(F("broadcast IP is "));
            Serial.println(getBroadcastIP());
            Serial.println(F("starting udp"));
#endif
            this->_udp.begin(UDP_PORT);
        }
#ifdef DEBUG
	} else {
        Serial.println(F("wifi ssid not found"));
#endif
    }

    return 0;
}

String WifiComm::generateJsonString() {
    String result("");
    serializeJson(this->_jsonDocument, result);
    return result;
}

void WifiComm::sendUdpBroadcastMessage(String msg) {
#ifdef DEBUG
    Serial.print(F("sending udp broadcast message: "));
    Serial.println(msg);
#endif
    this->_udp.beginPacket(this->getBroadcastIP().c_str(), UDP_PORT);
    this->_udp.print(msg.c_str());
    this->_udp.endPacket();
}

void WifiComm::sendUdpUnicastToServerMessage(String msg) {
    if (this->_serverIp.length() == 0) {
#ifdef DEBUG
        Serial.println(F("serverIP is empty"));
#endif
        return;
    }
#ifdef DEBUG
    Serial.print(F("sending udp unicast message: "));
    Serial.println(msg);
#endif
    this->_udp.beginPacket(this->_serverIp.c_str(), UDP_PORT);
    this->_udp.print(msg.c_str());
    this->_udp.endPacket();
}

void WifiComm::sendScanData(unsigned int frequency, unsigned int rssi) {
    this->_jsonDocument.clear();
    this->_jsonDocument["type"] = "scan";
    this->_jsonDocument["chipid"] = comm::CommTools::getChipIdAsString();
    this->_jsonDocument["frequency"] = frequency;
    this->_jsonDocument["rssi"] = rssi;
    this->sendUdpUnicastToServerMessage(this->generateJsonString());
    this->_udp.flush();
}

void WifiComm::sendFastRssiData(unsigned int rssi) {
    this->_jsonDocument.clear();
    this->_jsonDocument["type"] = "rssi";
    this->_jsonDocument["chipid"] = comm::CommTools::getChipIdAsString();
    this->_jsonDocument["rssi"] = rssi;
    this->sendUdpUnicastToServerMessage(this->generateJsonString());
}

void WifiComm::lap(unsigned long duration, unsigned int rssi) {
#ifdef DEBUG 
    Serial.println(F("sending lap message"));
#endif
    this->_jsonDocument.clear();
    this->_jsonDocument["type"] = "lap";
    this->_jsonDocument["chipid"] = comm::CommTools::getChipIdAsString();
    this->_jsonDocument["duration"] = duration;
    this->_jsonDocument["rssi"] = rssi;
    this->sendUdpUnicastToServerMessage(this->generateJsonString());
}

void WifiComm::processIncomingMessage() {
    int packetSize = this->_udp.parsePacket();
    if (packetSize) {
        char incomingPacket[1024];
        int len = this->_udp.read(incomingPacket, 1024);
        if (len > 0) {
            incomingPacket[len] = 0;
        }
#ifdef DEBUG
        Serial.printf("incoming packet: %s\n", incomingPacket);
#endif
        this->_jsonDocument.clear();
        DeserializationError error = deserializeJson(this->_jsonDocument, incomingPacket);
        if (error) {
#ifdef DEBUG
            Serial.print(F("deserializeJson() failed: "));
            Serial.println(error.c_str());
#endif
            String errorMsg = "json deserialize error: ";
            errorMsg += error.c_str();
            this->sendUdpBroadcastMessage(errorMsg);
            return;
        }

        if (strncmp(this->_jsonDocument["type"], "registerrequest", 15) == 0) {
#ifdef DEBUG
            Serial.println(F("got register request packet"));
#endif
            this->reg();
        } else if (strncmp(this->_jsonDocument["type"], "requestdata", 11) == 0) {
#ifdef DEBUG
            Serial.println(F("got request data packet"));
#endif
            this->sendData();
        } else if (strncmp(this->_jsonDocument["type"], "registerresponse", 16) == 0) {
#ifdef DEBUG
            Serial.println(F("got register response data packet"));
#endif
            // TODO check if chipid is equal to the board chipid
            String hostIp = this->_jsonDocument["hostIp"];
            this->_serverIp = hostIp;
        }
    }
}

void WifiComm::reg() {
#ifdef DEBUG 
    Serial.println(F("sending register message"));
#endif
    this->_jsonDocument.clear();
    this->_jsonDocument["type"] = "register32";
    this->_jsonDocument["chipid"] = comm::CommTools::getChipIdAsString();
    this->sendUdpBroadcastMessage(this->generateJsonString());
}

/*---------------------------------------------------
 * build broadcast ip for current localIP
 *-------------------------------------------------*/
String WifiComm::getBroadcastIP() {
    uint32_t ip = (uint32_t)WiFi.localIP();
    uint32_t netmask = (uint32_t)WiFi.subnetMask();
    uint32_t broadcast = ip | (~netmask);
    IPAddress broadcastAddress(broadcast);
    return broadcastAddress.toString();
}

void WifiComm::sendCalibrationDone() {
#ifdef DEBUG 
    Serial.println(F("sending calibration done message"));
#endif
    this->_jsonDocument.clear();
    this->_jsonDocument["type"] = "calibrationdone";
    this->_jsonDocument["chipid"] = comm::CommTools::getChipIdAsString();
    this->sendUdpUnicastToServerMessage(this->generateJsonString());
}

void WifiComm::sendVoltageAlarm(double voltage) {
#ifdef DEBUG 
    Serial.println(F("sending voltage alarm message"));
#endif
    this->_jsonDocument.clear();
    this->_jsonDocument["type"] = "battery_low";
    this->_jsonDocument["voltage"] = voltage;
    this->_jsonDocument["chipid"] = comm::CommTools::getChipIdAsString();
    this->sendUdpUnicastToServerMessage(this->generateJsonString());
}

void WifiComm::sendData() {
#ifdef DEBUG 
    Serial.println(F("sending data message"));
#endif
    this->sendUdpUnicastToServerMessage(comm::CommTools::getDeviceDataAsJsonStringFromStorage(this->_storage, this->_stateManager, this->_lapDetector, this->_batteryMgr, *this->_loopTime, this->_rssi, this->_version));
}

void WifiComm::disconnect() {
    this->_connected = false;
    WiFi.disconnect();
}