#include "wificomm.h"

using namespace comm;

//#define DEBUG
const unsigned int UDP_PORT = 31337;

WifiComm::WifiComm(util::Storage *storage) : Comm(storage), _wifiSsidFound(false) {

}

int WifiComm::connect() {
    this->_wifiSsidFound = false;
	this->_connected = false;

	int nrOfWifis = WiFi.scanNetworks();
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
#ifdef DEBUG
		Serial.print(F("wifi connecting to ssid "));
		Serial.print(this->_storage->getSsid());
#endif
		WiFi.begin(this->_storage->getSsid().c_str(), this->_storage->getWifiPassword().c_str());
		unsigned int wait = 0;
		while (WiFi.status() != WL_CONNECTED && wait < 15000) {
			delay(500);
			wait += 500;
#ifdef DEBUG
			Serial.print(F("."));
#endif
		}
		if (WiFi.status() != WL_CONNECTED) {
#ifdef DEBUG
			Serial.println(F("cannot connect to ssid, switching to standalone mode"));
#endif
		} else if (WiFi.status() == WL_CONNECTED) {
#ifdef DEBUG
			Serial.println(F("connected"));
#endif
			this->_connected = true;

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
		}
#ifdef DEBUG
	} else {
        Serial.println(F("wifi ssid not found"));
#endif
    }

    return 0;
}

void WifiComm::sendUdpMessage(String msg) {
#ifdef DEBUG
    Serial.print(F("sending udp message: "));
    Serial.println(msg);
#endif
    this->_udp.beginPacket(this->getBroadcastIP().c_str(), UDP_PORT);
    this->_udp.print(msg.c_str());
    this->_udp.endPacket();
}

void WifiComm::lap(unsigned long lapTime, unsigned int rssi) {
#ifdef DEBUG 
    Serial.println(F("sending lap message"));
#endif
    String msg = "{\"type\":\"lap\",\"chipid\":";
    msg += static_cast<unsigned long>(ESP.getEfuseMac());
    msg += ",\"duration\":";
    msg += lapTime;
    msg += ",\"rssi\":";
    msg += rssi;
    msg += "}";
    this->sendUdpMessage(msg);
}

void WifiComm::processIncommingMessage() {
    int packetSize = this->_udp.parsePacket();
    if (packetSize) {
        char incomingPacket[255];
        int len = this->_udp.read(incomingPacket, 255);
        if (len > 0) {
            incomingPacket[len] = 0;
        }
        if (strncmp(incomingPacket, "requestRegistration", 19) == 0) {
#ifdef DEBUG
            Serial.println(F("got request registration packet"));
#endif
            this->reg();
        }
    }
}

void WifiComm::reg() {
#ifdef DEBUG 
    Serial.println(F("sending register message"));
#endif
    String msg = "{\"type\":\"registerbt2\",\"chipid\":";
    msg += static_cast<unsigned long>(ESP.getEfuseMac());
    msg += ",\"ip\":";
    msg += WiFi.localIP();
    msg += "}";
    this->sendUdpMessage(msg);    
}

/*---------------------------------------------------
 * build broadcast ip for current localIP (just replaces last group with 255
 * may be wrong on non trivial setups but for now it is good enough
 *-------------------------------------------------*/
String WifiComm::getBroadcastIP() {
  String localIP = WiFi.localIP().toString();
  int pos = localIP.lastIndexOf('.');
  String broadcastIP = localIP.substring(0, pos);
  broadcastIP += ".255";
  return broadcastIP;
}